package com.alfred.kitabalhuda.utils

import android.content.Context
import androidx.preference.PreferenceManager as AndroidPreferenceManager

object PreferenceManager {
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    private const val KEY_AUTOPLAY_ENABLED = "autoplay_enabled"
    private const val KEY_PLAYBACK_SPEED = "playback_speed"
    private const val KEY_USE_ARABIC_INDIC = "use_arabic_indic"

    /**
     * Sauvegarde le temps de la dernière synchronisation.
     */
    fun saveLastSyncTime(context: Context, time: Long) {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, time).apply()
    }
    
    /**
     * Récupère le temps de la dernière synchronisation.
     */
    fun getLastSyncTime(context: Context): Long {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0)
    }

    fun setAutoplayEnabled(context: Context, enabled: Boolean) {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(KEY_AUTOPLAY_ENABLED, enabled).apply()
    }

    fun isAutoplayEnabled(context: Context): Boolean {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(KEY_AUTOPLAY_ENABLED, true)
    }

    fun getPlaybackSpeed(context: Context): Float {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getFloat(KEY_PLAYBACK_SPEED, 1.0f)
    }

    fun setPlaybackSpeed(context: Context, speed: Float) {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putFloat(KEY_PLAYBACK_SPEED, speed).apply()
    }

    fun isArabicIndicEnabled(context: Context): Boolean {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(KEY_USE_ARABIC_INDIC, true)
    }

    fun setArabicIndicEnabled(context: Context, enabled: Boolean) {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(KEY_USE_ARABIC_INDIC, enabled).apply()
    }

    fun saveString(context: Context, key: String, value: String) {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, defaultValue: String): String {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
}