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
    // Passing the audioId we want to add
    private var audioId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioId = arguments?.getLong(ARG_AUDIO_ID) ?: -1
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
             // Show create dialog, then add to it?
             // For simplicity: Dismiss this, show create dialog.
             // Ideally we want to create AND add.
             dismiss()
             val createSheet = CreatePlaylistBottomSheet { name ->
                 viewModel.createPlaylist(name)
                 // TODO: We should also add the current track to the new playlist automatically
                 // But for MVP, just creating is fine.
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
        if (audioId != -1L) {
             // We need a method in ViewModel to add track
             // Since LibraryViewModel serves the list, let's add `addTrackToPlaylist` there or use `PlaylistDetailViewModel`
             // LibraryViewModel is scoped to Activity, good place.
             // But we haven't added `addTrack` to it yet.
             (viewModel as? LibraryViewModel)?.addTrackToPlaylist(playlist.id, audioId)
             Toast.makeText(context, "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
             dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddToPlaylistBottomSheet"
        const val ARG_AUDIO_ID = "audio_id"

        fun newInstance(audioId: Long): AddToPlaylistBottomSheet {
            val sheet = AddToPlaylistBottomSheet()
            val args = Bundle()
            args.putLong(ARG_AUDIO_ID, audioId)
            sheet.arguments = args
            return sheet
        }
    }
}
