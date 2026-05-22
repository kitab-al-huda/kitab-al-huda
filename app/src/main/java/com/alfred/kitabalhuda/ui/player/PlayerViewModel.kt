package com.alfred.kitabalhuda.ui.player

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
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
                _player.value = controllerFuture.get()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
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

    fun playSurah(surahNumber: Int, surahName: String) {
        val controller = player.value ?: return

        viewModelScope.launch {
            val context = getApplication<Application>()
            val reciteurId = com.alfred.kitabalhuda.util.ReciterPreferences.getSelectedReciterId(context)
            val reciterName = com.alfred.kitabalhuda.util.ReciterPreferences.getSelectedReciterName(context)
            
            val database = (context as KitabAlHudaApplication).database
            val audioDao = database.audioDao()
            val sourateDao = database.sourateDao()

            val allSourates = withContext(Dispatchers.IO) { sourateDao.getAllSouratesDirect() }
            val allAudios = withContext(Dispatchers.IO) { audioDao.getAudiosByReciteurDirect(reciteurId) }

            if (allAudios.isNotEmpty() && allSourates.isNotEmpty()) {
                val mediaItems = allAudios.map { audio ->
                    val sourate = allSourates.find { it.numero == audio.sourateNumero }
                    val name = if (audio.partNumber > 1) {
                        "${sourate?.nomArabe ?: "سورة"} (${audio.partNumber})"
                    } else {
                        sourate?.nomArabe ?: "سورة"
                    }
                    val mediaUri = buildMediaUri(audio)
                    
                    MediaItem.Builder()
                        .setMediaId(mediaUri)
                        .setUri(android.net.Uri.parse(mediaUri))
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(name)
                                .setArtist(reciterName)
                                .build()
                        )
                        .build()
                }

                val startIndex = allAudios.indexOfFirst { it.sourateNumero == surahNumber }.coerceAtLeast(0)

                val selectedAudio = allAudios.getOrNull(startIndex)
                if (selectedAudio != null) {
                    historyDao.addToHistory(
                        com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity(audioId = selectedAudio.id)
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
                val mediaItems = allAudios.map { audio ->
                    val sourate = allSourates.find { it.numero == audio.sourateNumero }
                    val name = if (audio.partNumber > 1) {
                        "${sourate?.nomArabe ?: "سورة"} (${audio.partNumber})"
                    } else {
                        sourate?.nomArabe ?: "سورة"
                    }
                    val mediaUri = buildMediaUri(audio)
                    
                    MediaItem.Builder()
                        .setMediaId(mediaUri)
                        .setUri(android.net.Uri.parse(mediaUri))
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(name)
                                .setArtist(reciterName)
                                .build()
                        )
                        .build()
                }

                val startIndex = allAudios.indexOfFirst { it.sourateNumero == surahNumber }.coerceAtLeast(0)

                val selectedAudio = allAudios.getOrNull(startIndex)
                if (selectedAudio != null) {
                    historyDao.addToHistory(
                        com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity(audioId = selectedAudio.id)
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
            // Add first track to history
            if (startIndex < tracks.size) {
                historyDao.addToHistory(
                    com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity(
                        audioId = tracks[startIndex].audio.id
                    )
                )
            }
            
            val mediaItems = tracks.map { track ->
                val mediaUri = buildMediaUri(track.audio)
                MediaItem.Builder()
                    .setMediaId(mediaUri)
                    .setUri(android.net.Uri.parse(mediaUri))
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(track.sourate.nomPhonetique)
                            .setArtist(track.reciteur.nom)
                            .build()
                    )
                    .build()
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
