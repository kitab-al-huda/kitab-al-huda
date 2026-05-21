package com.alfred.kitabalhuda.ui.player

/**
 * Custom playback modes that extend beyond Media3's built-in repeat modes.
 *
 * Media3 only supports 3 repeat modes (OFF, ALL, ONE), but we need 4:
 * - SEQUENTIAL: Play all tracks in order, then stop at the end (maps to Player.REPEAT_MODE_OFF)
 * - REPEAT_ALL: Repeat the entire playlist (maps to Player.REPEAT_MODE_ALL)
 * - REPEAT_ONE: Repeat the current track (maps to Player.REPEAT_MODE_ONE)
 * - PLAY_CURRENT_AND_STOP: Play the current surah to completion, then stop (custom behavior)
 */
enum class PlaybackMode {
    /** Play all tracks sequentially, stop at end of playlist */
    SEQUENTIAL,
    /** Repeat all tracks in a loop */
    REPEAT_ALL,
    /** Repeat the current track in a loop */
    REPEAT_ONE,
    /** Play the current surah once and stop — does not advance to next track */
    PLAY_CURRENT_AND_STOP
}
