package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistTrack
import com.alfred.kitabalhuda.ui.player.PlayerUiState
import com.alfred.kitabalhuda.ui.player.PlayerViewModel
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme

class PlaylistDetailFragment : Fragment() {

    private lateinit var viewModel: PlaylistDetailViewModel
    private lateinit var playerViewModel: PlayerViewModel

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
        viewModel = ViewModelProvider(this)[PlaylistDetailViewModel::class.java]
        playerViewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KitabAlHudaTheme {
                    val tracks by viewModel.getPlaylistTracks(playlistId).observeAsState(emptyList())
                    val playerUiState by playerViewModel.playerUiState.observeAsState(PlayerUiState.Idle)

                    val currentMediaId = when (val s = playerUiState) {
                        is PlayerUiState.Playing -> s.mediaId
                        is PlayerUiState.Paused -> s.mediaId
                        else -> null
                    }
                    val isAudioPlaying = playerUiState is PlayerUiState.Playing
                    val bottomPadding = if (playerUiState !is PlayerUiState.Idle) 64 else 0

                    PlaylistDetailScreen(
                        playlistName = playlistName,
                        tracks = tracks,
                        currentPlayingMediaId = currentMediaId,
                        isAudioPlaying = isAudioPlaying,
                        onBackClick = { parentFragmentManager.popBackStack() },
                        onPlayAllClick = {
                            if (tracks.isNotEmpty()) {
                                playerViewModel.playPlaylist(tracks, 0)
                                Toast.makeText(
                                    context,
                                    getString(R.string.playing_playlist, playlistName),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onTrackClick = { track ->
                            val startIndex = tracks.indexOf(track)
                            if (startIndex >= 0) {
                                playerViewModel.playPlaylist(tracks, startIndex)
                                Toast.makeText(
                                    context,
                                    getString(R.string.playing_from, track.sourate.nomArabe),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onDeleteTrackClick = { track ->
                            viewModel.removeTrack(track.item)
                        },
                        onMoveTrackUpClick = { index ->
                            if (index > 0) {
                                val currentList = tracks.toMutableList()
                                val moved = currentList.removeAt(index)
                                currentList.add(index - 1, moved)
                                viewModel.updateOrder(currentList)
                            }
                        },
                        onMoveTrackDownClick = { index ->
                            if (index < tracks.size - 1) {
                                val currentList = tracks.toMutableList()
                                val moved = currentList.removeAt(index)
                                currentList.add(index + 1, moved)
                                viewModel.updateOrder(currentList)
                            }
                        },
                        bottomPadding = bottomPadding
                    )
                }
            }
        }
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
