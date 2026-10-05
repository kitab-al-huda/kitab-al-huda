package com.alfred.kitabalhuda.ui.quran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import com.alfred.kitabalhuda.repository.ReciteurRepository
import com.alfred.kitabalhuda.ui.theme.GoldPrimary
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme
import com.alfred.kitabalhuda.ui.theme.TealAccent
import com.alfred.kitabalhuda.ui.theme.TealContainer
import com.alfred.kitabalhuda.ui.theme.TealDark
import com.alfred.kitabalhuda.utils.ReciterPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReciterSelectionBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ReciterSelectionBottomSheet"

        const val RESULT_RECITER_ID = "reciterId"
        const val RESULT_RECITER_NAME = "reciterName"
        const val RESULT_SOURATE_NUMERO = "sourateNumero"
        const val RESULT_SOURATE_NOM = "sourateNom"

        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_SOURATE_NUMERO = "arg_sourate_numero"
        private const val ARG_SOURATE_NOM = "arg_sourate_nom"

        fun newInstance(
            requestKey: String = "reciterSelectionRequest",
            sourateNumero: Int = -1,
            sourateNom: String? = null
        ): ReciterSelectionBottomSheet {
            val sheet = ReciterSelectionBottomSheet()
            val args = Bundle().apply {
                putString(ARG_REQUEST_KEY, requestKey)
                putInt(ARG_SOURATE_NUMERO, sourateNumero)
                putString(ARG_SOURATE_NOM, sourateNom)
            }
            sheet.arguments = args
            return sheet
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val app = requireActivity().application as KitabAlHudaApplication
        val repository = ReciteurRepository(app.database.reciteurDao())

        val requestKey = arguments?.getString(ARG_REQUEST_KEY) ?: "reciterSelectionRequest"
        val sourateNumero = arguments?.getInt(ARG_SOURATE_NUMERO) ?: -1
        val sourateNom = arguments?.getString(ARG_SOURATE_NOM)
        val currentReciterId = ReciterPreferences.getSelectedReciterId(requireContext())

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KitabAlHudaTheme {
                    val reciters by repository.getAllReciteurs().collectAsState(initial = emptyList())

                    ReciterSelectionContent(
                        reciters = reciters,
                        selectedReciterId = currentReciterId,
                        onReciterSelected = { reciter ->
                            ReciterPreferences.setSelectedReciter(requireContext(), reciter.id, reciter.nom)

                            val result = Bundle().apply {
                                putInt(RESULT_RECITER_ID, reciter.id)
                                putString(RESULT_RECITER_NAME, reciter.nom)
                                putInt(RESULT_SOURATE_NUMERO, sourateNumero)
                                putString(RESULT_SOURATE_NOM, sourateNom)
                            }
                            parentFragmentManager.setFragmentResult(requestKey, result)
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    override fun getTheme(): Int = R.style.Theme_KitabAlHuda_BottomSheet
}

@Composable
private fun ReciterSelectionContent(
    reciters: List<ReciteurEntity>,
    selectedReciterId: Int,
    onReciterSelected: (ReciteurEntity) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.choose_reciter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                items(reciters, key = { it.id }) { reciter ->
                    val isSelected = reciter.id == selectedReciterId
                    val isZeroRated = reciter.id == 1 || reciter.id == 6

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onReciterSelected(reciter) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TealContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Reciter",
                                tint = if (isSelected) TealAccent else GoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = reciter.nom,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (isZeroRated) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = TealDark,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.badge_free),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TealAccent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (!reciter.description.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = reciter.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
