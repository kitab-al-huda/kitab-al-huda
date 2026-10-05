package com.alfred.kitabalhuda.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme

class MiniPlayerFragment : Fragment() {

    private var lastClickTime = 0L
    private lateinit var viewModel: PlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KitabAlHudaTheme {
                    val uiState by viewModel.playerUiState.observeAsState(PlayerUiState.Idle)
                    MiniPlayer(
                        uiState = uiState,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onClick = {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime < 1000) return@MiniPlayer
                            lastClickTime = currentTime

                            val existing = parentFragmentManager.findFragmentByTag("FullPlayer")
                            if (existing != null && existing.isAdded) return@MiniPlayer

                            val fullPlayer = FullPlayerFragment()
                            fullPlayer.show(parentFragmentManager, "FullPlayer")
                        }
                    )
                }
            }
        }
    }
}
