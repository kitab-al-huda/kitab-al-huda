package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.databinding.BottomSheetAddToPlaylistBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddToPlaylistBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddToPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LibraryViewModel

    // All audio-part IDs for the surah being added (1 item for single-part, N items for multi-part)
    private var audioIds: List<Long> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioIds = arguments?.getLongArray(ARG_AUDIO_IDS)?.toList() ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]

        setupRecyclerView()

        binding.btnNewPlaylist.setOnClickListener {
            dismiss()
            val createSheet = CreatePlaylistBottomSheet { name ->
                viewModel.createPlaylist(name)
                // TODO: Automatically add the current tracks to the new playlist after creation
            }
            createSheet.show(parentFragmentManager, CreatePlaylistBottomSheet.TAG)
        }
    }

    private fun setupRecyclerView() {
        val adapter = PlaylistMiniAdapter { playlist ->
            addToPlaylist(playlist)
        }
        binding.recyclerPlaylistsMini.layoutManager = LinearLayoutManager(context)
        binding.recyclerPlaylistsMini.adapter = adapter

        viewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
            adapter.submitList(playlists)
        }
    }

    private fun addToPlaylist(playlist: PlaylistEntity) {
        if (audioIds.isNotEmpty()) {
            // Adds all parts of the surah so multi-part surahs play fully from the playlist
            viewModel.addTracksToPlaylist(playlist.id, audioIds)
            val msg = if (audioIds.size > 1) {
                "Added to ${playlist.name} (${audioIds.size} parts)"
            } else {
                "Added to ${playlist.name}"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddToPlaylistBottomSheet"
        const val ARG_AUDIO_IDS = "audio_ids"

        /**
         * Creates a new instance for a surah.
         * Pass ALL part audio IDs so multi-part surahs (e.g. Al-Baqara with 5 parts)
         * are saved completely into the playlist.
         */
        fun newInstance(audioIds: List<Long>): AddToPlaylistBottomSheet {
            val sheet = AddToPlaylistBottomSheet()
            val args = Bundle()
            args.putLongArray(ARG_AUDIO_IDS, audioIds.toLongArray())
            sheet.arguments = args
            return sheet
        }
    }
}
