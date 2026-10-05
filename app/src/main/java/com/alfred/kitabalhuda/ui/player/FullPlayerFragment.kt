package com.alfred.kitabalhuda.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme
import com.alfred.kitabalhuda.utils.toArabicIndic
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FullPlayerFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: PlayerViewModel
    private var isSleepTimerActiveState by mutableStateOf(false)

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
                    val progress by viewModel.playerProgress.observeAsState(PlayerProgress(0L, 0L))
                    val playbackMode by viewModel.playbackMode.observeAsState(PlaybackMode.SEQUENTIAL)

                    FullPlayerScreen(
                        uiState = uiState,
                        progress = progress,
                        playbackMode = playbackMode,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onPrevious = { viewModel.seekToPrevSurah() },
                        onNext = { viewModel.seekToNextSurah() },
                        onSeek = { positionMs -> viewModel.seekTo(positionMs) },
                        onToggleShuffle = { viewModel.toggleShuffleMode() },
                        onSelectPlaybackMode = {
                            val currentMode = viewModel.playbackMode.value ?: PlaybackMode.SEQUENTIAL
                            val bottomSheet = PlaybackModeBottomSheet(currentMode) { mode ->
                                viewModel.setPlaybackMode(mode)
                            }
                            bottomSheet.show(parentFragmentManager, PlaybackModeBottomSheet.TAG)
                        },
                        onSleepTimerClick = {
                            val bottomSheet = SleepTimerBottomSheet { minutes ->
                                viewModel.setSleepTimer(minutes)
                                val message = if (minutes > 0) {
                                    getString(R.string.sleep_timer_set, minutes.toArabicIndic())
                                } else {
                                    getString(R.string.sleep_timer_off)
                                }
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                isSleepTimerActiveState = minutes > 0
                            }
                            bottomSheet.show(parentFragmentManager, SleepTimerBottomSheet.TAG)
                        },
                        onChangeReciterClick = {
                            val sheet = com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.newInstance(
                                requestKey = REQUEST_CHANGE_RECITER_FULL
                            )
                            sheet.show(parentFragmentManager, com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.TAG)
                        },
                        onCollapse = { dismiss() },
                        isSleepTimerActive = isSleepTimerActiveState
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFragmentResultListeners()
    }

    override fun getTheme(): Int = R.style.Theme_KitabAlHuda_BottomSheet

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true

                val layoutParams = sheet.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                sheet.layoutParams = layoutParams
            }
        }
    }

    private fun setupFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            REQUEST_CHANGE_RECITER_FULL,
            viewLifecycleOwner
        ) { _, bundle ->
            val reciterId = bundle.getInt(com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.RESULT_RECITER_ID)
            val reciterName = bundle.getString(com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.RESULT_RECITER_NAME) ?: return@setFragmentResultListener

            val currentTitle = viewModel.getCurrentSurahName()
            val surahNo = viewModel.getCurrentSurahNumber()
            if (currentTitle != null && surahNo > 0) {
                com.alfred.kitabalhuda.utils.ReciterPreferences.setSelectedReciter(
                    requireContext(), reciterId, reciterName
                )
                viewModel.playSurahWithReciter(surahNo, reciterId, reciterName)
            }
        }
    }

    companion object {
        private const val REQUEST_CHANGE_RECITER_FULL = "changeReciterFullPlayerRequest"
    }
}

