package com.alfred.kitabalhuda.util

import android.content.Context
import android.content.SharedPreferences

object ReciterPreferences {
    
    private const val PREFS_NAME = "reciter_prefs"
    private const val KEY_RECITER_ID = "selected_reciter_id"
    private const val KEY_RECITER_NAME = "selected_reciter_name"
    
    private const val DEFAULT_RECITER_ID = 1
    private const val DEFAULT_RECITER_NAME = "مشاري بن راشد العفاسي"
    
    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getSelectedReciterId(context: Context): Int {
        return prefs(context).getInt(KEY_RECITER_ID, DEFAULT_RECITER_ID)
    }
    
    fun getSelectedReciterName(context: Context): String {
        return prefs(context).getString(KEY_RECITER_NAME, DEFAULT_RECITER_NAME) ?: DEFAULT_RECITER_NAME
    }
    
    fun setSelectedReciter(context: Context, id: Int, name: String) {
        prefs(context).edit()
            .putInt(KEY_RECITER_ID, id)
            .putString(KEY_RECITER_NAME, name)
            .apply()
    }
}
