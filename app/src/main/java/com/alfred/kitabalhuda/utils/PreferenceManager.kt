package com.alfred.kitabalhuda.utils

import android.content.Context
import androidx.preference.PreferenceManager as AndroidPreferenceManager

object PreferenceManager {
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    private const val KEY_AUTOPLAY_ENABLED = "autoplay_enabled"
    private const val KEY_PREFERRED_VIDEO_QUALITY = "preferred_video_quality"

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

    fun savePreferredVideoQuality(context: Context, quality: String) {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(KEY_PREFERRED_VIDEO_QUALITY, quality).apply()
    }

    fun getPreferredVideoQuality(context: Context): String {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(KEY_PREFERRED_VIDEO_QUALITY, "HD") ?: "HD" // Default to HD
    }

    fun setAutoplayEnabled(context: Context, enabled: Boolean) {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(KEY_AUTOPLAY_ENABLED, enabled).apply()
    }

    fun isAutoplayEnabled(context: Context): Boolean {
        val prefs = AndroidPreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(KEY_AUTOPLAY_ENABLED, true)
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