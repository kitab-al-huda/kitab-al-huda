package com.alfred.kitabalhuda.ui.player

import android.app.Application
import android.content.ComponentName
import android.util.Log
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.repository.AudioRepository
import com.alfred.kitabalhuda.service.AudioPlayerService
import com.alfred.kitabalhuda.utils.PreferenceManager
import com.alfred.kitabalhuda.utils.toArabicIndic
import androidx.preference.PreferenceManager as AndroidXPreferenceManager
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private var controllerFuture: ListenableFuture<MediaController>
    private val _player = MutableLiveData<Player?>()

    private val _playbackMode = MutableLiveData(PlaybackMode.SEQUENTIAL)
    val playbackMode: LiveData<PlaybackMode> = _playbackMode

    private val _playerUiState = MutableLiveData<PlayerUiState>(PlayerUiState.Idle)
    val playerUiState: LiveData<PlayerUiState> = _playerUiState

    private val _playerProgress = MutableLiveData(PlayerProgress(0L, 0L))
    val playerProgress: LiveData<PlayerProgress> = _playerProgress

    private val audioRepository: AudioRepository
    private val historyDao: com.alfred.kitabalhuda.database.dao.ListeningHistoryDao

    private var positionUpdateJob: Job? = null

    private var lastSurateNo: Int = -1

    init {
        val database = (application as KitabAlHudaApplication).database
        audioRepository = AudioRepository(database.audioDao())
        historyDao = database.listeningHistoryDao()

        val sessionToken = SessionToken(
            application,
            ComponentName(application, AudioPlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                _player.value = controller
                if (controller != null) {
                    attachPlayerListener(controller)
                    applyPlaybackSpeed(controller)
                }
            } catch (e: Exception) {
                _playerUiState.value = PlayerUiState.Error(
                    e.localizedMessage ?: application.getString(R.string.msg_connect_failed)
                )
                Log.e("PlayerViewModel", "Failed to connect to player service", e)
            }
        }, MoreExecutors.directExecutor())

        // Apply playback speed changes on-the-fly
        val prefs = AndroidXPreferenceManager.getDefaultSharedPreferences(application)
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "playback_speed") {
                _player.value?.let { applyPlaybackSpeed(it) }
            }
        }
    }

    // ── Player listener ──────────────────────────────────────────────────

    private fun attachPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerUiState(player)
                updatePlayerProgress(player)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayerUiState(player)
                if (isPlaying) startPositionUpdates(player)
                else stopPositionUpdates()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val newSurateNo = mediaItem?.mediaMetadata?.extras
                    ?.getInt("sourateNumero", -1) ?: -1
                val isSameSurah = newSurateNo != -1 && newSurateNo == lastSurateNo
                lastSurateNo = newSurateNo

                if (!isSameSurah && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
                    && _playbackMode.value == PlaybackMode.PLAY_CURRENT_AND_STOP
                ) {
                    player.pause()
                }

                updatePlayerUiState(player)
                updatePlayerProgress(player)
            }

            override fun onPlayerError(error: PlaybackException) {
                _playerUiState.value = PlayerUiState.Error(
                    error.localizedMessage ?: "Playback error"
                )
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updatePlayerUiState(player)
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                if (error == null) {
                    updatePlayerUiState(player)
                    updatePlayerProgress(player)
                }
            }
        })
        lastSurateNo = getCurrentExtras(player)?.getInt("sourateNumero", -1) ?: -1
        updatePlayerUiState(player)
        updatePlayerProgress(player)
        if (player.isPlaying) startPositionUpdates(player)
    }

    // ── State updates ────────────────────────────────────────────────────

    private fun updatePlayerUiState(player: Player) {
        _playerUiState.value = when (player.playbackState) {
            Player.STATE_BUFFERING -> PlayerUiState.Loading
            Player.STATE_READY -> {
                val fullTitle = player.mediaMetadata.title?.toString() ?: ""
                val artist = player.mediaMetadata.artist?.toString() ?: ""
                val extras = player.currentMediaItem?.mediaMetadata?.extras
                val totalParts = extras?.getInt("totalParts", 1) ?: 1
                val title = if (totalParts > 1) fullTitle.substringBefore(" — ") else fullTitle
                val shuffle = player.shuffleModeEnabled
                val mediaId = player.currentMediaItem?.mediaId
                val surahNumber = extras?.getInt("sourateNumero", -1) ?: -1
                if (player.isPlaying) PlayerUiState.Playing(title, fullTitle, artist, shuffle, mediaId, surahNumber)
                else PlayerUiState.Paused(title, fullTitle, artist, shuffle, mediaId, surahNumber)
            }
            Player.STATE_IDLE, Player.STATE_ENDED -> PlayerUiState.Idle
            else -> PlayerUiState.Idle
        }
    }

    private fun updatePlayerProgress(player: Player) {
        if (player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING) {
            _playerProgress.value = PlayerProgress(
                currentPositionMs = getDisplayPosition(player),
                totalDurationMs = getSurahTotalMs(player)
            )
        }
    }

    private fun startPositionUpdates(player: Player) {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                _playerProgress.postValue(PlayerProgress(
                    currentPositionMs = getDisplayPosition(player),
                    totalDurationMs = getSurahTotalMs(player)
                ))
                delay(500)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    // ── Combined progress helpers (from FullPlayerFragment) ────────────────

    private fun getCurrentExtras(player: Player): android.os.Bundle? =
        player.currentMediaItem?.mediaMetadata?.extras

    private fun getDisplayPosition(player: Player): Long {
        val cumStart = getCurrentExtras(player)?.getLong("cumulativeStartMs", 0L) ?: 0L
        return cumStart + player.currentPosition.coerceAtLeast(0L)
    }

    private fun getSurahTotalMs(player: Player): Long {
        val stored = getCurrentExtras(player)?.getLong("surahTotalMs", 0L) ?: 0L
        if (stored > 0) return stored
        val live = player.duration
        return if (live > 0 && live != androidx.media3.common.C.TIME_UNSET) live else 0L
    }

    fun seekTo(positionMs: Long) {
        val player = _player.value ?: return
        val extras = getCurrentExtras(player)
        val currentSurateNo = extras?.getInt("sourateNumero", -1) ?: -1
        val surahTotalMs = extras?.getLong("surahTotalMs", 0L) ?: 0L

        if (currentSurateNo < 0 || surahTotalMs == 0L) {
            player.seekTo(positionMs)
            return
        }

        var surahStartIdx = player.currentMediaItemIndex
        while (surahStartIdx > 0) {
            val prevExtras = player.getMediaItemAt(surahStartIdx - 1).mediaMetadata.extras
            if (prevExtras?.getInt("sourateNumero", -1) == currentSurateNo) surahStartIdx--
            else break
        }

        var idx = surahStartIdx
        while (idx < player.mediaItemCount) {
            val itemExtras = player.getMediaItemAt(idx).mediaMetadata.extras
            if (itemExtras?.getInt("sourateNumero", -1) != currentSurateNo) break

            val cumStart = itemExtras.getLong("cumulativeStartMs", 0L)
            val partDur = itemExtras.getLong("partDurationMs", 0L)
            val nextCumStart = cumStart + partDur
            val isLastPart = idx + 1 >= player.mediaItemCount ||
                    player.getMediaItemAt(idx + 1).mediaMetadata.extras
                        ?.getInt("sourateNumero", -1) != currentSurateNo

            if (positionMs < nextCumStart || isLastPart) {
                val offset = (positionMs - cumStart).coerceAtLeast(0L)
                player.seekTo(idx, offset)
                return
            }
            idx++
        }
    }

    private fun applyPlaybackSpeed(player: Player) {
        val speed = PreferenceManager.getPlaybackSpeed(getApplication())
        player.setPlaybackSpeed(speed)
    }

    // ── Playback control ─────────────────────────────────────────────────

    fun play() {
        (_player.value as? MediaController)?.play()
    }

    fun pause() {
        (_player.value as? MediaController)?.pause()
    }

    fun togglePlayPause() {
        val player = _player.value ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekToNext() {
        (_player.value as? MediaController)?.seekToNextMediaItem()
    }

    fun seekToPrevious() {
        (_player.value as? MediaController)?.seekToPreviousMediaItem()
    }

    fun seekToNextSurah() {
        val player = _player.value ?: return
        val currentSurateNo = getCurrentExtras(player)?.getInt("sourateNumero", -1) ?: -1
        if (currentSurateNo < 0) { player.seekToNextMediaItem(); return }

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
    }

    fun seekToPrevSurah() {
        val player = _player.value ?: return
        val currentSurateNo = getCurrentExtras(player)?.getInt("sourateNumero", -1) ?: -1
        if (currentSurateNo < 0) { player.seekToPreviousMediaItem(); return }

        var idx = player.currentMediaItemIndex - 1
        while (idx >= 0) {
            if (player.getMediaItemAt(idx).mediaMetadata.extras
                    ?.getInt("sourateNumero", -1) != currentSurateNo
            ) break
            idx--
        }

        if (idx < 0) {
            player.seekTo(0, 0)
            return
        }

        val prevSurateNo = player.getMediaItemAt(idx).mediaMetadata.extras
            ?.getInt("sourateNumero", -1)
        while (idx > 0) {
            val prevExtras = player.getMediaItemAt(idx - 1).mediaMetadata.extras
            if (prevExtras?.getInt("sourateNumero", -1) != prevSurateNo) break
            idx--
        }
        player.seekTo(idx, 0)
    }

    fun setShuffleMode(enabled: Boolean) {
        (_player.value as? MediaController)?.shuffleModeEnabled = enabled
    }

    fun toggleShuffleMode() {
        val player = _player.value ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    // ── Existing public API ──────────────────────────────────────────────

    fun setPlaybackMode(mode: PlaybackMode) {
        _playbackMode.value = mode
        val player = _player.value ?: return
        when (mode) {
            PlaybackMode.SEQUENTIAL -> player.repeatMode = Player.REPEAT_MODE_OFF
            PlaybackMode.REPEAT_ALL -> player.repeatMode = Player.REPEAT_MODE_ALL
            PlaybackMode.REPEAT_ONE -> player.repeatMode = Player.REPEAT_MODE_ONE
            PlaybackMode.PLAY_CURRENT_AND_STOP -> player.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    private fun buildMediaUri(audio: AudioEntity): String {
        return if (!audio.fbMessageId.isNullOrEmpty()) {
            "${AudioPlayerService.MESSENGER_URI_SCHEME}${audio.fbMessageId}"
        } else {
            audio.urlWeb
        }
    }

    private fun buildTrackTitle(surahName: String?, partNumber: Int, totalParts: Int): String {
        val name = surahName ?: getApplication<KitabAlHudaApplication>().getString(R.string.surah_fallback)
        return if (totalParts > 1) "$name — الجزء ${partNumber.toArabicIndic()}/${totalParts.toArabicIndic()}" else name
    }

    private fun buildCumulativeMaps(
        audios: List<AudioEntity>
    ): Pair<Map<Long, Long>, Map<Int, Long>> {
        val cumulativeStartMap = mutableMapOf<Long, Long>()
        val surahTotalMap = mutableMapOf<Int, Long>()
        for ((surateNo, parts) in audios.groupBy { it.sourateNumero }) {
            var cumulative = 0L
            for (part in parts.sortedBy { it.partNumber }) {
                cumulativeStartMap[part.id] = cumulative
                cumulative += part.duree
            }
            surahTotalMap[surateNo] = cumulative
        }
        return Pair(cumulativeStartMap, surahTotalMap)
    }

    private fun buildMediaItem(
        audio: AudioEntity,
        title: String,
        artist: String,
        totalParts: Int,
        cumulativeStartMs: Long,
        surahTotalMs: Long
    ): MediaItem {
        val extras = android.os.Bundle().apply {
            putInt("sourateNumero", audio.sourateNumero)
            putInt("partNumber", audio.partNumber)
            putInt("totalParts", totalParts)
            putLong("cumulativeStartMs", cumulativeStartMs)
            putLong("surahTotalMs", surahTotalMs)
            putLong("partDurationMs", audio.duree)
        }
        val mediaUri = buildMediaUri(audio)
        return MediaItem.Builder()
            .setMediaId(mediaUri)
            .setUri(android.net.Uri.parse(mediaUri))
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setExtras(extras)
                    .build()
            )
            .build()
    }

    fun playSurah(surahNumber: Int) {
        val controller = _player.value ?: return

        viewModelScope.launch {
            val context = getApplication<Application>()
            val reciteurId = com.alfred.kitabalhuda.utils.ReciterPreferences.getSelectedReciterId(context)
            val reciterName = com.alfred.kitabalhuda.utils.ReciterPreferences.getSelectedReciterName(context)

            val database = (context as KitabAlHudaApplication).database
            val audioDao = database.audioDao()
            val sourateDao = database.sourateDao()

            val allSourates = withContext(Dispatchers.IO) { sourateDao.getAllSouratesDirect() }
            val allAudios = withContext(Dispatchers.IO) { audioDao.getAudiosByReciteurDirect(reciteurId) }

            if (allAudios.isNotEmpty() && allSourates.isNotEmpty()) {
                val partCounts = allAudios.groupBy { it.sourateNumero }
                    .mapValues { (_, parts) -> parts.size }
                val (cumulativeStartMap, surahTotalMap) = buildCumulativeMaps(allAudios)

                val mediaItems = allAudios.map { audio ->
                    val sourate = allSourates.find { it.numero == audio.sourateNumero }
                    val totalParts = partCounts[audio.sourateNumero] ?: 1
                    buildMediaItem(
                        audio = audio,
                        title = buildTrackTitle(sourate?.nomArabe, audio.partNumber, totalParts),
                        artist = reciterName,
                        totalParts = totalParts,
                        cumulativeStartMs = cumulativeStartMap[audio.id] ?: 0L,
                        surahTotalMs = surahTotalMap[audio.sourateNumero] ?: audio.duree
                    )
                }

                val startIndex = allAudios.indexOfFirst { it.sourateNumero == surahNumber }.coerceAtLeast(0)
                allAudios.getOrNull(startIndex)?.let { audio ->
                    historyDao.addToHistory(
                        com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity(audioId = audio.id)
                    )
                }

                controller.setMediaItems(mediaItems, startIndex, 0)
                controller.prepare()
                controller.play()
                applyPlaybackSpeed(controller)
            } else {
                android.util.Log.e("PlayerViewModel", "No audios or sourates found for reciter $reciteurId")
            }
        }
    }

    fun playSurahWithReciter(surahNumber: Int, reciterId: Int, reciterName: String) {
        val controller = _player.value ?: return

        viewModelScope.launch {
            val context = getApplication<Application>()
            val database = (context as KitabAlHudaApplication).database
            val audioDao = database.audioDao()
            val sourateDao = database.sourateDao()

            val allSourates = withContext(Dispatchers.IO) { sourateDao.getAllSouratesDirect() }
            val allAudios = withContext(Dispatchers.IO) { audioDao.getAudiosByReciteurDirect(reciterId) }

            if (allAudios.isNotEmpty() && allSourates.isNotEmpty()) {
                val partCounts = allAudios.groupBy { it.sourateNumero }
                    .mapValues { (_, parts) -> parts.size }
                val (cumulativeStartMap, surahTotalMap) = buildCumulativeMaps(allAudios)

                val mediaItems = allAudios.map { audio ->
                    val sourate = allSourates.find { it.numero == audio.sourateNumero }
                    val totalParts = partCounts[audio.sourateNumero] ?: 1
                    buildMediaItem(
                        audio = audio,
                        title = buildTrackTitle(sourate?.nomArabe, audio.partNumber, totalParts),
                        artist = reciterName,
                        totalParts = totalParts,
                        cumulativeStartMs = cumulativeStartMap[audio.id] ?: 0L,
                        surahTotalMs = surahTotalMap[audio.sourateNumero] ?: audio.duree
                    )
                }

                val startIndex = allAudios.indexOfFirst { it.sourateNumero == surahNumber }.coerceAtLeast(0)
                allAudios.getOrNull(startIndex)?.let { audio ->
                    historyDao.addToHistory(
                        com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity(audioId = audio.id)
                    )
                }

                controller.setMediaItems(mediaItems, startIndex, 0)
                controller.prepare()
                controller.play()
                applyPlaybackSpeed(controller)
            } else {
                android.util.Log.e("PlayerViewModel", "No audios or sourates found for reciter $reciterId")
            }
        }
    }

    fun playPlaylist(tracks: List<com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistTrack>, startIndex: Int = 0) {
        val controller = _player.value ?: return

        if (tracks.isEmpty()) {
            android.util.Log.e("PlayerViewModel", "Playlist is empty")
            return
        }

        viewModelScope.launch {
            if (startIndex < tracks.size) {
                historyDao.addToHistory(
                    com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity(
                        audioId = tracks[startIndex].audio.id
                    )
                )
            }

            val audioEntities = tracks.map { it.audio }

            val groupKey: (com.alfred.kitabalhuda.database.entity.AudioEntity) -> Pair<Int, Int> =
                { audio -> Pair(audio.sourateNumero, audio.reciteurId) }

            val partCountsByKey = audioEntities
                .groupBy(groupKey)
                .mapValues { (_, parts) -> parts.size }

            val cumulativeStartMap = mutableMapOf<Long, Long>()
            val surahTotalByKey = mutableMapOf<Pair<Int, Int>, Long>()
            for ((key, parts) in audioEntities.groupBy(groupKey)) {
                var cumulative = 0L
                for (part in parts.sortedBy { it.partNumber }) {
                    cumulativeStartMap[part.id] = cumulative
                    cumulative += part.duree
                }
                surahTotalByKey[key] = cumulative
            }

            val mediaItems = tracks.map { track ->
                val key = groupKey(track.audio)
                val totalParts = partCountsByKey[key] ?: 1
                buildMediaItem(
                    audio = track.audio,
                    title = buildTrackTitle(track.sourate.nomArabe, track.audio.partNumber, totalParts),
                    artist = track.reciteur.nom,
                    totalParts = totalParts,
                    cumulativeStartMs = cumulativeStartMap[track.audio.id] ?: 0L,
                    surahTotalMs = surahTotalByKey[key] ?: track.audio.duree
                )
            }

            controller.setMediaItems(mediaItems, startIndex, 0)
            controller.prepare()
            controller.play()
            applyPlaybackSpeed(controller)
        }
    }

    fun setSleepTimer(minutes: Int) {
        val controller = (_player.value as? MediaController) ?: return
        val command = if (minutes > 0) {
            AudioPlayerService.COMMAND_START_SLEEP_TIMER
        } else {
            AudioPlayerService.COMMAND_STOP_SLEEP_TIMER
        }
        val args = android.os.Bundle().apply {
            putInt(AudioPlayerService.EXTRA_MINUTES, minutes)
        }
        controller.sendCustomCommand(androidx.media3.session.SessionCommand(command, android.os.Bundle.EMPTY), args)
    }

    fun getCurrentSurahNumber(): Int {
        val player = _player.value ?: return -1
        return getCurrentExtras(player)?.getInt("sourateNumero", -1) ?: -1
    }

    fun getCurrentSurahName(): String? {
        return _player.value?.mediaMetadata?.title?.toString()
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionUpdates()
        MediaController.releaseFuture(controllerFuture)
    }
}
