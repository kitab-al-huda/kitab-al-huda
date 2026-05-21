package com.alfred.kitabalhuda.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.databinding.FragmentFullPlayerBinding
import java.util.concurrent.TimeUnit

class FullPlayerFragment : Fragment() {

    private var _binding: FragmentFullPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PlayerViewModel
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressAction = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 1000)
        }
    }

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
    }

    private fun setupUI() {
        binding.btnCollapse.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnFullPlay.setOnClickListener {
            viewModel.player.value?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }

        binding.btnFullNext.setOnClickListener {
            viewModel.player.value?.seekToNext()
        }

        binding.btnFullPrev.setOnClickListener {
            viewModel.player.value?.seekToPrevious()
        }

        binding.btnRepeatMode.setOnClickListener {
            val currentMode = viewModel.playbackMode.value ?: PlaybackMode.SEQUENTIAL
            // Cycle through: SEQUENTIAL → REPEAT_ALL → REPEAT_ONE → PLAY_CURRENT_AND_STOP → SEQUENTIAL
            val newMode = when (currentMode) {
                PlaybackMode.SEQUENTIAL -> PlaybackMode.REPEAT_ALL
                PlaybackMode.REPEAT_ALL -> PlaybackMode.REPEAT_ONE
                PlaybackMode.REPEAT_ONE -> PlaybackMode.PLAY_CURRENT_AND_STOP
                PlaybackMode.PLAY_CURRENT_AND_STOP -> PlaybackMode.SEQUENTIAL
            }
            viewModel.setPlaybackMode(newMode)
            updateRepeatModeButton(newMode)
        }
        
        binding.btnShuffle.setOnClickListener {
            viewModel.player.value?.let { player ->
                player.shuffleModeEnabled = !player.shuffleModeEnabled
                updateShuffleButton(player.shuffleModeEnabled)
            }
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
            val sheet = com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.newInstance()
            sheet.onReciterSelected = { reciter ->
                // Replay current surah with the new reciter
                viewModel.player.value?.let { player ->
                    val currentTitle = player.mediaMetadata.title?.toString()
                    if (currentTitle != null) {
                        // Save as new default
                        com.alfred.kitabalhuda.util.ReciterPreferences.setSelectedReciter(
                            requireContext(), reciter.id, reciter.nom
                        )
                        // Get current surah number from media metadata extras or replay by name
                        viewModel.player.value?.let { p ->
                            val surahName = p.mediaMetadata.title?.toString() ?: return@let
                            viewModel.playSurahWithReciter(
                                // Try to determine surah number — for now use position+1 as fallback
                                p.currentMediaItemIndex + 1,
                                surahName,
                                reciter.id,
                                reciter.nom
                            )
                        }
                    }
                }
            }
            sheet.show(parentFragmentManager, com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.TAG)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.textPosition.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateProgressAction)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    viewModel.player.value?.seekTo(it.progress.toLong())
                    handler.post(updateProgressAction)
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.player.observe(viewLifecycleOwner) { player ->
            if (player != null) {
                updatePlayerUI(player)
                player.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updatePlayPauseButton(isPlaying)
                        if (isPlaying) {
                            handler.post(updateProgressAction)
                        } else {
                            handler.removeCallbacks(updateProgressAction)
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                        updateMetadata(player)
                        // Update duration when new track loads
                        if (player.duration != androidx.media3.common.C.TIME_UNSET && player.duration > 0) {
                            _binding?.textDuration?.text = formatTime(player.duration)
                            _binding?.seekBar?.max = player.duration.toInt()
                        }
                        // PLAY_CURRENT_AND_STOP: pause when current surah ends and auto-advances
                        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
                            && viewModel.playbackMode.value == PlaybackMode.PLAY_CURRENT_AND_STOP) {
                            player.pause()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY, Player.STATE_BUFFERING -> {
                                if (player.duration != androidx.media3.common.C.TIME_UNSET && player.duration > 0) {
                                    _binding?.textDuration?.text = formatTime(player.duration)
                                    _binding?.seekBar?.max = player.duration.toInt()
                                }
                                if (playbackState == Player.STATE_READY) {
                                    updateMetadata(player)
                                }
                            }
                        }
                    }
                })
                
                // Initial update
                updatePlayPauseButton(player.isPlaying)
                updateMetadata(player)
                updateRepeatModeButton(viewModel.playbackMode.value ?: PlaybackMode.SEQUENTIAL)
                updateShuffleButton(player.shuffleModeEnabled)
                if (player.isPlaying) handler.post(updateProgressAction)
            }
        }
    }

    private fun updatePlayerUI(player: Player) {
        // Initial setup if needed
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        _binding?.btnFullPlay?.setImageResource(
            if (isPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play
        )
    }

    private fun updateMetadata(player: Player) {
        val metadata = player.mediaMetadata
        _binding?.textFullTitle?.text = metadata.title ?: "Unknown Title"
        _binding?.textFullArtist?.text = metadata.artist ?: "Unknown Artist"
    }
    
    private fun updateRepeatModeButton(mode: PlaybackMode) {
        _binding?.let { binding ->
            when (mode) {
                PlaybackMode.SEQUENTIAL -> {
                    binding.btnRepeatMode.setImageResource(R.drawable.ic_play_sequential)
                    binding.btnRepeatMode.setColorFilter(
                        requireContext().getColor(R.color.gray_500)
                    )
                    binding.btnRepeatMode.contentDescription = "تشغيل متتابع"
                }
                PlaybackMode.REPEAT_ALL -> {
                    binding.btnRepeatMode.setImageResource(R.drawable.ic_repeat)
                    binding.btnRepeatMode.setColorFilter(
                        requireContext().getColor(R.color.huda_gold)
                    )
                    binding.btnRepeatMode.contentDescription = "تكرار الكل"
                }
                PlaybackMode.REPEAT_ONE -> {
                    binding.btnRepeatMode.setImageResource(R.drawable.ic_repeat_one)
                    binding.btnRepeatMode.setColorFilter(
                        requireContext().getColor(R.color.huda_gold)
                    )
                    binding.btnRepeatMode.contentDescription = "تكرار السورة"
                }
                PlaybackMode.PLAY_CURRENT_AND_STOP -> {
                    binding.btnRepeatMode.setImageResource(R.drawable.ic_play_once_stop)
                    binding.btnRepeatMode.setColorFilter(
                        requireContext().getColor(R.color.huda_gold)
                    )
                    binding.btnRepeatMode.contentDescription = "تشغيل السورة الحالية والتوقف"
                }
            }
        }
    }
    
    private fun updateShuffleButton(isShuffleEnabled: Boolean) {
        _binding?.btnShuffle?.setColorFilter(
            requireContext().getColor(
                if (isShuffleEnabled) R.color.huda_gold else R.color.gray_500
            )
        )
    }

    private fun updateSleepTimerButton(isActive: Boolean) {
        _binding?.btnSleepTimer?.setColorFilter(
            requireContext().getColor(
                if (isActive) R.color.huda_gold else R.color.gray_500
            )
        )
    }

    private fun updateProgress() {
        viewModel.player.value?.let { player ->
            val position = player.currentPosition
            val duration = player.duration
            
            _binding?.seekBar?.progress = position.toInt()
            _binding?.textPosition?.text = formatTime(position)
            
            // Update duration if it's valid and different from what's displayed
            if (duration != androidx.media3.common.C.TIME_UNSET && duration > 0) {
                _binding?.textDuration?.text = formatTime(duration)
                _binding?.seekBar?.max = duration.toInt()
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateProgressAction)
        _binding = null
    }
}
