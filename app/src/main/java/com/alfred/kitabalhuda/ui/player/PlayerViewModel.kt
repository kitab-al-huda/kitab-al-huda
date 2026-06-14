package com.alfred.kitabalhuda.ui.player

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.repository.AudioRepository
import com.alfred.kitabalhuda.service.AudioPlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private var controllerFuture: ListenableFuture<MediaController>
    private val _player = MutableLiveData<Player?>()
    val player: LiveData<Player?> = _player

    // Custom playback mode (extends beyond Media3's 3 built-in repeat modes)
    private val _playbackMode = MutableLiveData(PlaybackMode.SEQUENTIAL)
    val playbackMode: LiveData<PlaybackMode> = _playbackMode

    private val _playerUiState = MutableLiveData<PlayerUiState>(PlayerUiState.Idle)
    val playerUiState: LiveData<PlayerUiState> = _playerUiState

    private val audioRepository: AudioRepository
    private val historyDao: com.alfred.kitabalhuda.database.dao.ListeningHistoryDao

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
                }
            } catch (e: Exception) {
                _playerUiState.value = PlayerUiState.Error(
                    e.localizedMessage ?: "Failed to connect to player service"
                )
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun attachPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerUiState(player)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayerUiState(player)
            }

            override fun onPlayerError(error: PlaybackException) {
                _playerUiState.value = PlayerUiState.Error(
                    error.localizedMessage ?: "Playback error"
                )
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                if (error == null) updatePlayerUiState(player)
            }
        })
        updatePlayerUiState(player)
    }

    private fun updatePlayerUiState(player: Player) {
        _playerUiState.value = when (player.playbackState) {
            Player.STATE_BUFFERING -> PlayerUiState.Loading
            Player.STATE_READY -> {
                val title = player.mediaMetadata.title?.toString() ?: ""
                val artist = player.mediaMetadata.artist?.toString() ?: ""
                if (player.isPlaying) PlayerUiState.Playing(title, artist)
                else PlayerUiState.Paused(title, artist)
            }
            Player.STATE_IDLE, Player.STATE_ENDED -> PlayerUiState.Idle
            else -> PlayerUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaController.releaseFuture(controllerFuture)
    }

    /**
     * Sets the custom playback mode and maps it to the underlying Media3 repeat mode.
     * For PLAY_CURRENT_AND_STOP, we use REPEAT_MODE_OFF at the player level,
     * and the fragment handles pausing when a media item transition occurs.
     */
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

    /**
     * Construit l'URI de lecture pour un AudioEntity.
     * Si l'audio a un fbMessageId, on utilise le schéma messenger://
     * qui sera résolu par AudioPlayerService en URL CDN temporaire.
     */
    private fun buildMediaUri(audio: AudioEntity): String {
        return if (!audio.fbMessageId.isNullOrEmpty()) {
            "${AudioPlayerService.MESSENGER_URI_SCHEME}${audio.fbMessageId}"
        } else {
            audio.urlWeb
        }
    }

    /**
     * Builds a human-readable title for a track.
     * - Single part  : "البقرة"
     * - Multi-part   : "البقرة — الجزء 2/5"
     */
    private fun buildTrackTitle(surahName: String?, partNumber: Int, totalParts: Int): String {
        val name = surahName ?: "سورة"
        return if (totalParts > 1) "$name — الجزء $partNumber/$totalParts" else name
    }

    /**
     * Pre-computes cumulative start offsets and total durations per surah.
     * Returns a pair of:
     *   - Map<audioId, cumulativeStartMs>  — offset of this part in the combined timeline
     *   - Map<sourateNumero, surahTotalMs> — total duration of the full surah
     */
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

    /** Build a [MediaItem] with all the extra metadata the UI needs. */
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

    @Suppress("UNUSED_PARAMETER")
    fun playSurah(surahNumber: Int, surahName: String) {
        val controller = player.value ?: return

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
            } else {
                android.util.Log.e("PlayerViewModel", "No audios or sourates found for reciter $reciteurId")
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun playSurahWithReciter(surahNumber: Int, surahName: String, reciterId: Int, reciterName: String) {
        val controller = player.value ?: return

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
            } else {
                android.util.Log.e("PlayerViewModel", "No audios or sourates found for reciter $reciterId")
            }
        }
    }

    fun playPlaylist(tracks: List<com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistTrack>, startIndex: Int = 0) {
        val controller = player.value ?: return

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

            // Group by (sourateNumero, reciteurId) so playlists that mix different reciters
            // for the same surah don't pollute each other's part counts / cumulative offsets.
            val groupKey: (com.alfred.kitabalhuda.database.entity.AudioEntity) -> Pair<Int, Int> =
                { audio -> Pair(audio.sourateNumero, audio.reciteurId) }

            val partCountsByKey = audioEntities
                .groupBy(groupKey)
                .mapValues { (_, parts) -> parts.size }

            // Build cumulative start offsets for each part within its (sourate, reciter) group
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
                    title = buildTrackTitle(track.sourate.nomPhonetique, track.audio.partNumber, totalParts),
                    artist = track.reciteur.nom,
                    totalParts = totalParts,
                    cumulativeStartMs = cumulativeStartMap[track.audio.id] ?: 0L,
                    surahTotalMs = surahTotalByKey[key] ?: track.audio.duree
                )
            }

            controller.setMediaItems(mediaItems, startIndex, 0)
            controller.prepare()
            controller.play()
        }
    }

    fun setSleepTimer(minutes: Int) {
        val controller = (player.value as? MediaController) ?: return
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
}
