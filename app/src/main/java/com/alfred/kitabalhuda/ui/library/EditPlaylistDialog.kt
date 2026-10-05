package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.TextButton
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
import androidx.fragment.app.DialogFragment
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.ui.theme.BackgroundDark
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme
import com.alfred.kitabalhuda.ui.theme.TealAccent

class EditPlaylistDialog : DialogFragment() {

    private var playlistId: Int = -1
    private var playlistName: String = ""
    private var playlistDescription: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistId = arguments?.getInt(ARG_ID) ?: -1
        playlistName = arguments?.getString(ARG_NAME) ?: ""
        playlistDescription = arguments?.getString(ARG_DESCRIPTION)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KitabAlHudaTheme {
                    EditPlaylistContent(
                        initialName = playlistName,
                        initialDescription = playlistDescription ?: "",
                        onSave = { newName, newDescription ->
                            val result = Bundle().apply {
                                putInt(RESULT_ID, playlistId)
                                putString(RESULT_NAME, newName)
                                putString(RESULT_DESCRIPTION, newDescription)
                            }
                            parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
                            dismiss()
                        },
                        onCancel = { dismiss() }
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "EditPlaylistDialog"
        const val REQUEST_KEY = "editPlaylistRequest"
        const val RESULT_ID = "playlistId"
        const val RESULT_NAME = "newName"
        const val RESULT_DESCRIPTION = "newDescription"

        private const val ARG_ID = "arg_id"
        private const val ARG_NAME = "arg_name"
        private const val ARG_DESCRIPTION = "arg_description"

        fun newInstance(id: Int, name: String, description: String?): EditPlaylistDialog {
            val dialog = EditPlaylistDialog()
            val args = Bundle().apply {
                putInt(ARG_ID, id)
                putString(ARG_NAME, name)
                putString(ARG_DESCRIPTION, description)
            }
            dialog.arguments = args
            return dialog
        }
    }
}

@Composable
private fun EditPlaylistContent(
    initialName: String,
    initialDescription: String,
    onSave: (String, String?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.edit_playlist),
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(id = R.string.description_optional)) },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealAccent,
                    focusedLabelColor = TealAccent,
                    cursorColor = TealAccent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val trimmed = name.trim()
                        if (trimmed.isNotEmpty()) {
                            onSave(trimmed, description.trim().ifEmpty { null })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealAccent,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.save),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
