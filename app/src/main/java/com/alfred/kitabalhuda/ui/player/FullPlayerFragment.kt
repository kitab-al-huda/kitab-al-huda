package com.alfred.kitabalhuda.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.lifecycle.ViewModelProvider
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.databinding.FragmentFullPlayerBinding
import java.util.concurrent.TimeUnit

class FullPlayerFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFullPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]
        setupUI()
        observeViewModel()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUI() {
        binding.btnCollapse.setOnClickListener {
            dismiss()
        }

        binding.btnFullPlay.setOnClickListener {
            viewModel.togglePlayPause()
        }

        binding.btnFullNext.setOnClickListener {
            viewModel.seekToNextSurah()
        }

        binding.btnFullPrev.setOnClickListener {
            viewModel.seekToPrevSurah()
        }

        binding.btnRepeatMode.setOnClickListener {
            val currentMode = viewModel.playbackMode.value ?: PlaybackMode.SEQUENTIAL
            val bottomSheet = PlaybackModeBottomSheet(currentMode) { mode ->
                viewModel.setPlaybackMode(mode)
            }
            bottomSheet.show(parentFragmentManager, PlaybackModeBottomSheet.TAG)
        }

        binding.btnShuffle.setOnClickListener {
            viewModel.toggleShuffleMode()
        }

        binding.btnSleepTimer.setOnClickListener {
            val bottomSheet = SleepTimerBottomSheet { minutes ->
                viewModel.setSleepTimer(minutes)
                val message = if (minutes > 0) {
                    getString(R.string.sleep_timer_set, minutes)
                } else {
                    getString(R.string.sleep_timer_off)
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                updateSleepTimerButton(minutes > 0)
            }
            bottomSheet.show(parentFragmentManager, SleepTimerBottomSheet.TAG)
        }

        binding.btnChangeReciter.setOnClickListener {
            val sheet = com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.newInstance(requestKey = REQUEST_CHANGE_RECITER_FULL)
            sheet.show(parentFragmentManager, com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.TAG)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    _binding?.textPosition?.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let { bar ->
                    viewModel.seekTo(bar.progress.toLong())
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.playerUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlayerUiState.Loading -> {
                    _binding?.progressFullLoading?.visibility = View.VISIBLE
                    _binding?.btnFullPlay?.visibility = View.INVISIBLE
                }
                is PlayerUiState.Playing -> {
                    _binding?.progressFullLoading?.visibility = View.GONE
                    _binding?.btnFullPlay?.visibility = View.VISIBLE
                    _binding?.btnFullPlay?.setImageResource(R.drawable.ic_media_pause)
                    _binding?.textFullTitle?.text = state.title
                    _binding?.textFullArtist?.text = state.artist
                    updateShuffleButton(state.shuffleModeEnabled)
                }
                is PlayerUiState.Paused -> {
                    _binding?.progressFullLoading?.visibility = View.GONE
                    _binding?.btnFullPlay?.visibility = View.VISIBLE
                    _binding?.btnFullPlay?.setImageResource(R.drawable.ic_media_play)
                    _binding?.textFullTitle?.text = state.title
                    _binding?.textFullArtist?.text = state.artist
                    updateShuffleButton(state.shuffleModeEnabled)
                }
                is PlayerUiState.Idle -> {
                    _binding?.progressFullLoading?.visibility = View.GONE
                    _binding?.btnFullPlay?.visibility = View.VISIBLE
                    _binding?.btnFullPlay?.setImageResource(R.drawable.ic_media_play)
                    _binding?.textFullTitle?.text = getString(R.string.ready_to_play)
                    _binding?.textFullArtist?.text = ""
                }
                is PlayerUiState.Error -> {
                    _binding?.progressFullLoading?.visibility = View.GONE
                    _binding?.btnFullPlay?.visibility = View.VISIBLE
                    _binding?.btnFullPlay?.setImageResource(R.drawable.ic_media_play)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.playerProgress.observe(viewLifecycleOwner) { progress ->
            _binding?.seekBar?.progress = progress.currentPositionMs.toInt()
            _binding?.textPosition?.text = formatTime(progress.currentPositionMs)
            if (progress.totalDurationMs > 0) {
                _binding?.textDuration?.text = formatTime(progress.totalDurationMs)
                if (_binding?.seekBar?.max != progress.totalDurationMs.toInt()) {
                    _binding?.seekBar?.max = progress.totalDurationMs.toInt()
                }
            }
        }

        viewModel.playbackMode.observe(viewLifecycleOwner) { mode ->
            updateRepeatModeButton(mode)
        }
    }

    // ── UI update helpers ────────────────────────────────────────────────

    private fun updateRepeatModeButton(mode: PlaybackMode) {
        _binding?.let { b ->
            when (mode) {
                PlaybackMode.SEQUENTIAL -> {
                    b.btnRepeatMode.setImageResource(R.drawable.ic_play_sequential)
                    b.btnRepeatMode.setColorFilter(requireContext().getColor(R.color.gray_500))
                    b.btnRepeatMode.contentDescription = "تشغيل متتابع"
                }
                PlaybackMode.REPEAT_ALL -> {
                    b.btnRepeatMode.setImageResource(R.drawable.ic_repeat)
                    b.btnRepeatMode.setColorFilter(requireContext().getColor(R.color.huda_gold))
                    b.btnRepeatMode.contentDescription = "تكرار الكل"
                }
                PlaybackMode.REPEAT_ONE -> {
                    b.btnRepeatMode.setImageResource(R.drawable.ic_repeat_one)
                    b.btnRepeatMode.setColorFilter(requireContext().getColor(R.color.huda_gold))
                    b.btnRepeatMode.contentDescription = "تكرار السورة"
                }
                PlaybackMode.PLAY_CURRENT_AND_STOP -> {
                    b.btnRepeatMode.setImageResource(R.drawable.ic_play_once_stop)
                    b.btnRepeatMode.setColorFilter(requireContext().getColor(R.color.huda_gold))
                    b.btnRepeatMode.contentDescription = "تشغيل السورة الحالية والتوقف"
                }
            }
        }
    }

    private fun updateShuffleButton(isEnabled: Boolean) {
        _binding?.btnShuffle?.setColorFilter(
            requireContext().getColor(if (isEnabled) R.color.huda_gold else R.color.gray_500)
        )
    }

    private fun updateSleepTimerButton(isActive: Boolean) {
        _binding?.btnSleepTimer?.setColorFilter(
            requireContext().getColor(if (isActive) R.color.huda_gold else R.color.gray_500)
        )
    }

    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
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
                viewModel.playSurahWithReciter(surahNo, currentTitle, reciterId, reciterName)
            }
        }
    }

    companion object {
        private const val REQUEST_CHANGE_RECITER_FULL = "changeReciterFullPlayerRequest"
    }
}
