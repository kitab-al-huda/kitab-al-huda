package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfred.kitabalhuda.databinding.FragmentPlaylistDetailBinding

class PlaylistDetailFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PlaylistDetailViewModel
    private lateinit var adapter: PlaylistDetailAdapter
    
    private var playlistId: Int = -1
    private var playlistName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistId = arguments?.getInt(ARG_PLAYLIST_ID) ?: -1
        playlistName = arguments?.getString(ARG_PLAYLIST_NAME) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PlaylistDetailViewModel::class.java]
        
        setupUI()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.title = playlistName
        binding.toolbar.setNavigationOnClickListener {
             parentFragmentManager.popBackStack()
        }
        
        // Play All button - plays entire playlist from beginning
        binding.btnPlayAll.setOnClickListener {
            val tracks = adapter.getCurrentTracks()
            if (tracks.isNotEmpty()) {
                val playerViewModel = ViewModelProvider(requireActivity())[com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java]
                playerViewModel.playPlaylist(tracks, 0)
                Toast.makeText(context, "Playing: $playlistName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        val playerViewModel = ViewModelProvider(requireActivity())[com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java]
        
        adapter = PlaylistDetailAdapter(
            onTrackClick = { track ->
                // Play from this track onwards
                val allTracks = adapter.getCurrentTracks()
                val startIndex = allTracks.indexOf(track)
                if (startIndex >= 0) {
                    playerViewModel.playPlaylist(allTracks, startIndex)
                    Toast.makeText(context, "Playing from ${track.sourate.nomPhonetique}", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { track ->
                viewModel.removeTrack(track.item)
            },
            onOrderChanged = { items ->
                viewModel.updateOrder(items)
            }
        )
        binding.recyclerTracks.layoutManager = LinearLayoutManager(context)
        binding.recyclerTracks.adapter = adapter
        
        val callback = PlaylistTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.recyclerTracks)
    }

    private fun observeViewModel() {
        if (playlistId != -1) {
            viewModel.getPlaylistTracks(playlistId).observe(viewLifecycleOwner) { tracks ->
                adapter.submitList(tracks)
                binding.textEmptyTracks.visibility = if (tracks.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        // Observe player to update currently playing indicator
        val playerViewModel = ViewModelProvider(requireActivity())[com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java]
        playerViewModel.player.observe(viewLifecycleOwner) { player ->
            player?.let {
                // Listen to media item changes
                it.addListener(object : androidx.media3.common.Player.Listener {
                    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                        adapter.setCurrentlyPlaying(mediaItem?.mediaId)
                    }
                })
                // Set initial state
                adapter.setCurrentlyPlaying(it.currentMediaItem?.mediaId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlist_id"
        const val ARG_PLAYLIST_NAME = "playlist_name"

        fun newInstance(id: Int, name: String): PlaylistDetailFragment {
            val fragment = PlaylistDetailFragment()
            val args = Bundle()
            args.putInt(ARG_PLAYLIST_ID, id)
            args.putString(ARG_PLAYLIST_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }
}
