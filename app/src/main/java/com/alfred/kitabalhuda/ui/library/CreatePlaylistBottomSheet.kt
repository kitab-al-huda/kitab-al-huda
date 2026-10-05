package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.ui.theme.BackgroundDark
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme
import com.alfred.kitabalhuda.ui.theme.TealAccent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreatePlaylistBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: LibraryViewModel
    private var audioIdsToAddToNew: List<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioIdsToAddToNew = arguments?.getLongArray(ARG_AUDIO_IDS)?.toList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KitabAlHudaTheme {
                    CreatePlaylistContent(
                        onCreatePlaylist = { name ->
                            val ids = audioIdsToAddToNew
                            if (ids != null && ids.isNotEmpty()) {
                                viewModel.createPlaylistWithTracks(name, ids)
                                Toast.makeText(context, getString(R.string.playlist_created), Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.createPlaylist(name)
                            }
                            dismiss()
                        },
                        onEmptyName = {
                            Toast.makeText(context, getString(R.string.enter_name_please), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    override fun getTheme(): Int = R.style.Theme_KitabAlHuda_BottomSheet

    companion object {
        const val TAG = "CreatePlaylistBottomSheet"
        private const val ARG_AUDIO_IDS = "audio_ids"

        fun newInstance(audioIds: List<Long>? = null): CreatePlaylistBottomSheet {
            val fragment = CreatePlaylistBottomSheet()
            if (audioIds != null) {
                val args = Bundle()
                args.putLongArray(ARG_AUDIO_IDS, audioIds.toLongArray())
                fragment.arguments = args
            }
            return fragment
        }
    }
}

@Composable
private fun CreatePlaylistContent(
    onCreatePlaylist: (String) -> Unit,
    onEmptyName: () -> Unit
) {
    var name by remember { mutableStateOf("") }

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
                text = stringResource(id = R.string.create_playlist),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.playlist_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealAccent,
                    focusedLabelColor = TealAccent,
                    cursorColor = TealAccent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isNotEmpty()) {
                        onCreatePlaylist(trimmed)
                    } else {
                        onEmptyName()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealAccent,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.create),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
