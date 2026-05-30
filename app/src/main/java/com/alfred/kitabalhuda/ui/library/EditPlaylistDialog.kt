package com.alfred.kitabalhuda.ui.library

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.entity.PlaylistEntity

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_edit_playlist, null)

        val editName = view.findViewById<EditText>(R.id.edit_playlist_name)
        val editDescription = view.findViewById<EditText>(R.id.edit_playlist_description)

        editName.setText(playlistName)
        editDescription.setText(playlistDescription ?: "")

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.edit_playlist)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = editName.text.toString().trim()
                val newDescription = editDescription.text.toString().trim().ifEmpty { null }
                if (newName.isNotEmpty()) {
                    val result = Bundle().apply {
                        putInt(RESULT_ID, playlistId)
                        putString(RESULT_NAME, newName)
                        putString(RESULT_DESCRIPTION, newDescription)
                    }
                    parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
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
