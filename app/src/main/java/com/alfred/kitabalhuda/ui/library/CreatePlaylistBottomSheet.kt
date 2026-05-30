package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.alfred.kitabalhuda.databinding.BottomSheetCreatePlaylistBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreatePlaylistBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreatePlaylistBinding? = null
    private val binding get() = _binding!!
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
        _binding = BottomSheetCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]

        binding.btnCreateConfirm.setOnClickListener {
            val name = binding.inputPlaylistName.editText?.text.toString().trim()
            if (name.isNotEmpty()) {
                val ids = audioIdsToAddToNew
                if (ids != null && ids.isNotEmpty()) {
                    viewModel.createPlaylistWithTracks(name, ids)
                    Toast.makeText(context, "Playlist created with selected tracks", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.createPlaylist(name)
                }
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
        private const val ARG_AUDIO_IDS = "arg_audio_ids"

        fun newInstance(audioIds: List<Long>? = null): CreatePlaylistBottomSheet {
            val fragment = CreatePlaylistBottomSheet()
            audioIds?.let {
                val args = Bundle().apply {
                    putLongArray(ARG_AUDIO_IDS, it.toLongArray())
                }
                fragment.arguments = args
            }
            return fragment
        }
    }
}
