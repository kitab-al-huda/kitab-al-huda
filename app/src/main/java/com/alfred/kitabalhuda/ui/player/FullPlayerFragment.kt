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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.databinding.FragmentFullPlayerBinding
import java.util.concurrent.TimeUnit

class FullPlayerFragment : Fragment() {

    private var _binding: FragmentFullPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PlayerViewModel
    private val handler = Handler(Looper.getMainLooper())

    /** Tracks the currently displayed sourateNumero to detect real surah changes. */
    private var lastSurateNumero: Int = -1

    private var playerListener: Player.Listener? = null
    private var currentPlayer: Player? = null

    private val updateProgressAction = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 500)
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────

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

        // Handle system back button to collapse player immediately without changing tabs underneath
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                }
            }
        )
    }

    override fun onDestroyView() {
        currentPlayer?.let { player ->
            playerListener?.let { listener ->
                player.removeListener(listener)
            }
        }
        playerListener = null
        currentPlayer = null
        super.onDestroyView()
        handler.removeCallbacks(updateProgressAction)
        _binding = null
    }

    // ──────────────────────────────────────────────────────────────────────
    // UI setup
    // ──────────────────────────────────────────────────────────────────────

    private fun setupUI() {
        binding.btnCollapse.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnFullPlay.setOnClickListener {
            viewModel.player.value?.let { player ->
                if (player.isPlaying) player.pause() else player.play()
            }
        }

        // Navigate to the first part of the NEXT surah
        binding.btnFullNext.setOnClickListener {
            viewModel.player.value?.let { seekToNextSurah(it) }
        }

        // Navigate to the first part of the PREVIOUS surah
        binding.btnFullPrev.setOnClickListener {
            viewModel.player.value?.let { seekToPrevSurah(it) }
        }

        binding.btnRepeatMode.setOnClickListener {
            val currentMode = viewModel.playbackMode.value ?: PlaybackMode.SEQUENTIAL
            val newMode = when (currentMode) {
                PlaybackMode.SEQUENTIAL       -> PlaybackMode.REPEAT_ALL
                PlaybackMode.REPEAT_ALL       -> PlaybackMode.REPEAT_ONE
                PlaybackMode.REPEAT_ONE       -> PlaybackMode.PLAY_CURRENT_AND_STOP
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
            val sheet = com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.newInstance(requestKey = REQUEST_CHANGE_RECITER_FULL)
            sheet.show(parentFragmentManager, com.alfred.kitabalhuda.ui.quran.ReciterSelectionBottomSheet.TAG)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    _binding?.textPosition?.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateProgressAction)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let { bar ->
                    viewModel.player.value?.let { player ->
                        seekToDisplayPosition(player, bar.progress.toLong())
                    }
                    handler.post(updateProgressAction)
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.player.observe(viewLifecycleOwner) { player ->
            currentPlayer?.let { oldPlayer ->
                playerListener?.let { listener ->
                    oldPlayer.removeListener(listener)
                }
            }

            currentPlayer = player

            if (player != null) {
                lastSurateNumero = getCurrentExtras(player)?.getInt("sourateNumero", -1) ?: -1

                val listener = object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updatePlayPauseButton(isPlaying)
                        if (isPlaying) handler.post(updateProgressAction)
                        else handler.removeCallbacks(updateProgressAction)
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val newSurateNo = mediaItem?.mediaMetadata?.extras
                            ?.getInt("sourateNumero", -1) ?: -1
                        val isSameSurah = newSurateNo != -1 && newSurateNo == lastSurateNumero
                        lastSurateNumero = newSurateNo

                        if (!isSameSurah) {
                            // Truly different surah — refresh title and seekbar range
                            updateMetadata(player)
                            val surahTotal = getSurahTotalMs(player)
                            if (surahTotal > 0) {
                                _binding?.textDuration?.text = formatTime(surahTotal)
                                _binding?.seekBar?.max = surahTotal.toInt()
                            }

                            // PLAY_CURRENT_AND_STOP: pause only on surah boundary, not on part boundary
                            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
                                && viewModel.playbackMode.value == PlaybackMode.PLAY_CURRENT_AND_STOP
                            ) {
                                player.pause()
                            }
                        }
                        // If isSameSurah (part transition): do nothing — progress bar continues seamlessly
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY, Player.STATE_BUFFERING -> {
                                // Prefer surahTotalMs from extras for multi-part surahs
                                val surahTotal = getSurahTotalMs(player)
                                if (surahTotal > 0) {
                                    _binding?.textDuration?.text = formatTime(surahTotal)
                                    _binding?.seekBar?.max = surahTotal.toInt()
                                }
                                if (playbackState == Player.STATE_READY) updateMetadata(player)
                            }
                        }
                    }
                }
                playerListener = listener
                player.addListener(listener)

                // Initial UI state
                updatePlayPauseButton(player.isPlaying)
                updateMetadata(player)
                updateRepeatModeButton(viewModel.playbackMode.value ?: PlaybackMode.SEQUENTIAL)
                updateShuffleButton(player.shuffleModeEnabled)
                val surahTotal = getSurahTotalMs(player)
                if (surahTotal > 0) {
                    _binding?.textDuration?.text = formatTime(surahTotal)
                    _binding?.seekBar?.max = surahTotal.toInt()
                }
                if (player.isPlaying) handler.post(updateProgressAction)
            } else {
                playerListener = null
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Combined progress helpers
    // ──────────────────────────────────────────────────────────────────────

    private fun getCurrentExtras(player: Player): android.os.Bundle? =
        player.currentMediaItem?.mediaMetadata?.extras

    /**
     * The position to display on the seekbar:
     * offset of this part within the surah + actual position within this part.
     */
    private fun getDisplayPosition(player: Player): Long {
        val cumStart = getCurrentExtras(player)?.getLong("cumulativeStartMs", 0L) ?: 0L
        return cumStart + player.currentPosition.coerceAtLeast(0L)
    }

    /**
     * The total duration to display:
     * surahTotalMs covers all parts; falls back to player.duration for single-part surahs.
     */
    private fun getSurahTotalMs(player: Player): Long {
        val stored = getCurrentExtras(player)?.getLong("surahTotalMs", 0L) ?: 0L
        if (stored > 0) return stored
        val live = player.duration
        return if (live > 0 && live != androidx.media3.common.C.TIME_UNSET) live else 0L
    }

    /**
     * Converts a seekbar position (in ms within the full surah) to a
     * (partIndex, offsetWithinPart) seek, then tells ExoPlayer to jump there.
     */
    private fun seekToDisplayPosition(player: Player, displayPositionMs: Long) {
        val extras = getCurrentExtras(player)
        val currentSurateNo = extras?.getInt("sourateNumero", -1) ?: -1
        val surahTotalMs = extras?.getLong("surahTotalMs", 0L) ?: 0L
        
        if (currentSurateNo < 0 || surahTotalMs == 0L) {
            // Si la durée totale est inconnue (duree = 0 dans la base de données), 
            // on fait un seek classique sur la partie en cours.
            player.seekTo(displayPositionMs)
            return
        }

        // Find the first MediaItem index belonging to this surah
        var surahStartIdx = player.currentMediaItemIndex
        while (surahStartIdx > 0) {
            val prevExtras = player.getMediaItemAt(surahStartIdx - 1).mediaMetadata.extras
            if (prevExtras?.getInt("sourateNumero", -1) == currentSurateNo) surahStartIdx--
            else break
        }

        // Walk forward through parts to find which one contains displayPositionMs
        var idx = surahStartIdx
        while (idx < player.mediaItemCount) {
            val itemExtras = player.getMediaItemAt(idx).mediaMetadata.extras
            if (itemExtras?.getInt("sourateNumero", -1) != currentSurateNo) break

            val cumStart     = itemExtras.getLong("cumulativeStartMs", 0L)
            val partDur      = itemExtras.getLong("partDurationMs", 0L)
            val nextCumStart = cumStart + partDur
            val isLastPart = idx + 1 >= player.mediaItemCount ||
                    player.getMediaItemAt(idx + 1).mediaMetadata.extras
                        ?.getInt("sourateNumero", -1) != currentSurateNo

            if (displayPositionMs < nextCumStart || isLastPart) {
                val offset = (displayPositionMs - cumStart).coerceAtLeast(0L)
                player.seekTo(idx, offset)
                return
            }
            idx++
        }
    }

    /**
     * Jump to the first part of the NEXT surah.
     */
    private fun seekToNextSurah(player: Player) {
        val currentSurateNo = getCurrentExtras(player)?.getInt("sourateNumero", -1) ?: -1
        if (currentSurateNo < 0) { player.seekToNext(); return }

        var idx = player.currentMediaItemIndex + 1
        while (idx < player.mediaItemCount) {
            val surateNo = player.getMediaItemAt(idx).mediaMetadata.extras
                ?.getInt("sourateNumero", -1)
            if (surateNo != currentSurateNo) {
                player.seekTo(idx, 0)
                return
            }
            idx++
        }
        // Already at last surah — no-op (or you can wrap around)
    }

    /**
     * Jump to the first part of the PREVIOUS surah.
     */
    private fun seekToPrevSurah(player: Player) {
        val currentSurateNo = getCurrentExtras(player)?.getInt("sourateNumero", -1) ?: -1
        if (currentSurateNo < 0) { player.seekToPrevious(); return }

        // Step back past all parts of the current surah
        var idx = player.currentMediaItemIndex - 1
        while (idx >= 0) {
            if (player.getMediaItemAt(idx).mediaMetadata.extras
                    ?.getInt("sourateNumero", -1) != currentSurateNo
            ) break
            idx--
        }

        if (idx < 0) {
            // Already at the very first surah — restart it
            player.seekTo(0, 0)
            return
        }

        // Now find the FIRST part of this previous surah
        val prevSurateNo = player.getMediaItemAt(idx).mediaMetadata.extras
            ?.getInt("sourateNumero", -1)
        while (idx > 0) {
            val prevExtras = player.getMediaItemAt(idx - 1).mediaMetadata.extras
            if (prevExtras?.getInt("sourateNumero", -1) != prevSurateNo) break
            idx--
        }
        player.seekTo(idx, 0)
    }

    // ──────────────────────────────────────────────────────────────────────
    // UI update helpers
    // ──────────────────────────────────────────────────────────────────────

    private fun updateMetadata(player: Player) {
        val extras = getCurrentExtras(player)
        val rawTitle = player.mediaMetadata.title?.toString() ?: "Unknown Title"
        // For multi-part surahs, strip the " — الجزء X/Y" suffix so the title
        // displayed in the UI always shows the surah name only.
        val surahName = if ((extras?.getInt("totalParts", 1) ?: 1) > 1) {
            rawTitle.substringBefore(" — ")
        } else {
            rawTitle
        }
        _binding?.textFullTitle?.text = surahName
        _binding?.textFullArtist?.text = player.mediaMetadata.artist ?: "Unknown Artist"
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        _binding?.btnFullPlay?.setImageResource(
            if (isPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play
        )
    }

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

    private fun updateProgress() {
        viewModel.player.value?.let { player ->
            val displayPos = getDisplayPosition(player)
            val displayDur = getSurahTotalMs(player)

            _binding?.seekBar?.progress = displayPos.toInt()
            _binding?.textPosition?.text = formatTime(displayPos)

            if (displayDur > 0) {
                _binding?.textDuration?.text = formatTime(displayDur)
                if (_binding?.seekBar?.max != displayDur.toInt()) {
                    _binding?.seekBar?.max = displayDur.toInt()
                }
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

            viewModel.player.value?.let { player ->
                val currentTitle = player.mediaMetadata.title?.toString()
                if (currentTitle != null) {
                    com.alfred.kitabalhuda.util.ReciterPreferences.setSelectedReciter(
                        requireContext(), reciterId, reciterName
                    )
                    val surahNo = getCurrentExtras(player)?.getInt("sourateNumero", 1) ?: 1
                    viewModel.playSurahWithReciter(surahNo, currentTitle, reciterId, reciterName)
                }
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    companion object {
        private const val REQUEST_CHANGE_RECITER_FULL = "changeReciterFullPlayerRequest"
    }
}
