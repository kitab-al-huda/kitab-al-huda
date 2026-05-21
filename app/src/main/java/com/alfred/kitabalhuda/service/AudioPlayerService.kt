package com.alfred.kitabalhuda.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class AudioPlayerService : MediaLibraryService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var sleepTimerRunnable: Runnable? = null

    companion object {
        const val COMMAND_START_SLEEP_TIMER = "START_SLEEP_TIMER"
        const val COMMAND_STOP_SLEEP_TIMER = "STOP_SLEEP_TIMER"
        const val EXTRA_MINUTES = "EXTRA_MINUTES"
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        
        mediaSession = MediaLibrarySession.Builder(this, player, LibrarySessionCallback()).build()
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
    }

    private fun fadeOutAndStop() {
        // Simple fade out
        val startVolume = player.volume
        val steps = 20
        val fadeDuration = 3000L
        val stepDelay = fadeDuration / steps

        object : android.os.CountDownTimer(fadeDuration, stepDelay) {
            override fun onTick(millisUntilFinished: Long) {
                player.volume = startVolume * (millisUntilFinished.toFloat() / fadeDuration)
            }
            override fun onFinish() {
                player.pause()
                player.volume = startVolume // Reset volume for next play
            }
        }.start()
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
            val updatedMediaItems = mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }
    }
}
