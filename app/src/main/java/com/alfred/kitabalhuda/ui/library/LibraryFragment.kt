package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.ui.player.PlayerUiState
import com.alfred.kitabalhuda.ui.player.PlayerViewModel
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme

class LibraryFragment : Fragment() {

    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var playerViewModel: PlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        libraryViewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]
        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        playerViewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]

        setupFragmentResultListeners()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KitabAlHudaTheme {
                    val playlistsWithCount by libraryViewModel.allPlaylistsWithCount.observeAsState(emptyList())
                    val historyItems by historyViewModel.recentHistory.observeAsState(emptyList())
                    val playerUiState by playerViewModel.playerUiState.observeAsState(PlayerUiState.Idle)

                    val bottomPadding = if (playerUiState !is PlayerUiState.Idle) 64 else 0

                    LibraryScreen(
                        playlistsWithCount = playlistsWithCount,
                        historyItems = historyItems,
                        onPlaylistClick = { playlist ->
                            val fragment = PlaylistDetailFragment.newInstance(playlist.id, playlist.name)
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment_activity_main, fragment)
                                .addToBackStack(null)
                                .commit()
                        },
                        onEditPlaylistClick = { playlist ->
                            val dialog = EditPlaylistDialog.newInstance(playlist.id, playlist.name, playlist.description)
                            dialog.show(parentFragmentManager, EditPlaylistDialog.TAG)
                        },
                        onDeletePlaylistClick = { playlist ->
                            AlertDialog.Builder(requireContext())
                                .setTitle(R.string.delete)
                                .setMessage(R.string.delete_playlist_confirmation)
                                .setPositiveButton(R.string.delete) { _, _ ->
                                    libraryViewModel.deletePlaylist(playlist)
                                }
                                .setNegativeButton(R.string.cancel, null)
                                .show()
                        },
                        onCreatePlaylistClick = {
                            val bottomSheet = CreatePlaylistBottomSheet.newInstance()
                            bottomSheet.show(parentFragmentManager, CreatePlaylistBottomSheet.TAG)
                        },
                        onHistoryItemClick = { historyItem ->
                            playerViewModel.playSurahWithReciter(
                                historyItem.sourate.numero,
                                historyItem.reciteur.id,
                                historyItem.reciteur.nom
                            )
                            Toast.makeText(
                                context,
                                getString(R.string.playing_surah, historyItem.sourate.nomArabe),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        bottomPadding = bottomPadding
                    )
                }
            }
        }
    }

    private fun setupFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            EditPlaylistDialog.REQUEST_KEY,
            this
        ) { _, bundle ->
            val id = bundle.getInt(EditPlaylistDialog.RESULT_ID)
            val newName = bundle.getString(EditPlaylistDialog.RESULT_NAME) ?: return@setFragmentResultListener
            val newDescription = bundle.getString(EditPlaylistDialog.RESULT_DESCRIPTION)

            libraryViewModel.allPlaylists.value?.find { it.id == id }?.let { original ->
                libraryViewModel.updatePlaylist(original.copy(name = newName, description = newDescription))
            }
        }
    }
}