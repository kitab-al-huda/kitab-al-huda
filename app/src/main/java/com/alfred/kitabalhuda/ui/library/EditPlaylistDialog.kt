package com.alfred.kitabalhuda.ui.library

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.entity.PlaylistEntity

class EditPlaylistDialog(
    private val playlist: PlaylistEntity,
    private val onSave: (String, String?) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_edit_playlist, null)
        
        val editName = view.findViewById<EditText>(R.id.edit_playlist_name)
        val editDescription = view.findViewById<EditText>(R.id.edit_playlist_description)
        
        editName.setText(playlist.name)
        editDescription.setText(playlist.description ?: "")
        
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.edit_playlist)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = editName.text.toString().trim()
                val newDescription = editDescription.text.toString().trim().ifEmpty { null }
                if (newName.isNotEmpty()) {
                    onSave(newName, newDescription)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
    
    companion object {
        const val TAG = "EditPlaylistDialog"
    }
}
