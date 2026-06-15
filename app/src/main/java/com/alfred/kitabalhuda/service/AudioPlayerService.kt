package com.alfred.kitabalhuda.service

import android.annotation.SuppressLint
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.LibraryResult
import com.alfred.kitabalhuda.repository.MessengerRepository
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.runBlocking
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
@SuppressLint("UnsafeOptInUsageError")
@OptIn(UnstableApi::class)
class AudioPlayerService : MediaLibraryService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession
    private lateinit var messengerRepository: MessengerRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var sleepTimerRunnable: Runnable? = null
    private var fadeOutTimer: android.os.CountDownTimer? = null

    companion object {
        private const val TAG = "AudioPlayerService"
        const val COMMAND_START_SLEEP_TIMER = "START_SLEEP_TIMER"
        const val COMMAND_STOP_SLEEP_TIMER = "STOP_SLEEP_TIMER"
        const val EXTRA_MINUTES = "EXTRA_MINUTES"
        const val MESSENGER_URI_SCHEME = "messenger://"
    }

    override fun onCreate() {
        super.onCreate()
        messengerRepository = MessengerRepository.getInstance(this)

        val defaultDataSourceFactory = DefaultDataSource.Factory(this)
        val resolvingDataSourceFactory = ResolvingDataSource.Factory(
            defaultDataSourceFactory
        ) { dataSpec ->
            val uri = dataSpec.uri
            if (uri.scheme == "messenger") {
                val messageId = uri.host ?: uri.path?.removePrefix("/") ?: uri.toString().removePrefix(MESSENGER_URI_SCHEME)
                val cdnUrl = runBlocking { messengerRepository.resolveAudioUrl(messageId) }
                if (cdnUrl != null) {
                    Log.d(TAG, "Lazy resolved messenger URI to CDN: ${cdnUrl.take(80)}...")
                    dataSpec.buildUpon().setUri(android.net.Uri.parse(cdnUrl)).build()
                } else {
                    dataSpec
                }
            } else {
                dataSpec
            }
        }

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this).setDataSourceFactory(resolvingDataSourceFactory))
            .build()
        
        mediaSession = MediaLibrarySession.Builder(this, player, LibrarySessionCallback()).build()

        // Handle the case where Android 12+ refuses to start the foreground service
        // (e.g. when the user-gesture token has expired before Media3 calls startForeground).
        // Without this listener the exception propagates uncaught and the notification
        // never appears, even though audio playback continues.
        setListener(object : MediaSessionService.Listener {
            override fun onForegroundServiceStartNotAllowedException() {
                Log.w(TAG, "Foreground service start not allowed – notification may be delayed. " +
                    "Audio playback will continue without the media notification until the " +
                    "app returns to the foreground.")
            }
        })
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
    }

    override fun onDestroy() {
        cancelSleepTimer()
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    private fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return

        val delayMillis = minutes * 60 * 1000L
        sleepTimerRunnable = Runnable {
            fadeOutAndStop()
        }
        handler.postDelayed(sleepTimerRunnable!!, delayMillis)
    }

    private fun cancelSleepTimer() {
        sleepTimerRunnable?.let { handler.removeCallbacks(it) }
        sleepTimerRunnable = null
        fadeOutTimer?.cancel()
        fadeOutTimer = null
    }

    private fun fadeOutAndStop() {
        // Simple fade out
        val startVolume = player.volume
        val steps = 20
        val fadeDuration = 3000L
        val stepDelay = fadeDuration / steps

        fadeOutTimer?.cancel()
        val timer = object : android.os.CountDownTimer(fadeDuration, stepDelay) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    player.volume = startVolume * (millisUntilFinished.toFloat() / fadeDuration)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            override fun onFinish() {
                try {
                    player.pause()
                    player.volume = startVolume // Reset volume for next play
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        fadeOutTimer = timer
        timer.start()
    }
    
    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(androidx.media3.session.SessionCommand(COMMAND_START_SLEEP_TIMER, android.os.Bundle.EMPTY))
                .add(androidx.media3.session.SessionCommand(COMMAND_STOP_SLEEP_TIMER, android.os.Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId("ROOT_ID")
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .setFolderType(androidx.media3.common.MediaMetadata.FOLDER_TYPE_MIXED)
                        .setTitle(applicationContext.getString(com.alfred.kitabalhuda.R.string.app_name))
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: androidx.media3.session.SessionCommand,
            args: android.os.Bundle
        ): ListenableFuture<androidx.media3.session.SessionResult> {
            when (customCommand.customAction) {
                COMMAND_START_SLEEP_TIMER -> {
                    val minutes = args.getInt(EXTRA_MINUTES, 0)
                    startSleepTimer(minutes)
                }
                COMMAND_STOP_SLEEP_TIMER -> {
                    cancelSleepTimer()
                }
            }
            return Futures.immediateFuture(androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_SUCCESS))
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            // Lazy Loading: on conserve l'URI messenger:// tel quel.
            // Il sera résolu juste avant la lecture par le ResolvingDataSource.
            val updatedMediaItems = mediaItems.map { item ->
                val mediaId = item.mediaId
                if (mediaId.startsWith(MESSENGER_URI_SCHEME)) {
                    item.buildUpon().setUri(android.net.Uri.parse(mediaId)).build()
                } else {
                    item.buildUpon().setUri(mediaId).build()
                }
            }.toMutableList()
            
            return Futures.immediateFuture(updatedMediaItems)
        }
    }
}
