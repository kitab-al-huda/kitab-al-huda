package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.alfred.kitabalhuda.databinding.BottomSheetCreatePlaylistBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreatePlaylistBottomSheet(
    private val onConfirm: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreateConfirm.setOnClickListener {
            val name = binding.inputPlaylistName.editText?.text.toString().trim()
            if (name.isNotEmpty()) {
                onConfirm(name)
                dismiss()
            } else {
                Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreatePlaylistBottomSheet"
    }
}
