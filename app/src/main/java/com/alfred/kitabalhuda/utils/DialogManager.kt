package com.alfred.kitabalhuda.utils

import android.content.Context
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.FragmentActivity
import com.alfred.kitabalhuda.network.DialogConfig
import com.alfred.kitabalhuda.network.DialogDef
import com.alfred.kitabalhuda.ui.dialogs.RemoteDialogFragment
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

object DialogManager {

    private const val TAG = "DialogManager"
    private const val PREFS_NAME = "dialog_tracker"
    private const val DIALOGS_URL = "https://kitab-al-huda.github.io/data/dialogs.json"
    private const val JSON_TIMEOUT_SECONDS = 10L

    private val gson = Gson()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(JSON_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(JSON_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    suspend fun showIfNeeded(activity: FragmentActivity) {
        try {
            val json = withContext(Dispatchers.IO) {
                val request = Request.Builder().url(DIALOGS_URL).build()
                httpClient.newCall(request).execute().body()?.string() ?: return@withContext null
            } ?: return

            val config = gson.fromJson(json, DialogConfig::class.java)
            val appVersionCode = getAppVersionCode(activity)

            for (dialog in config.dialogs) {
                if (shouldShow(dialog, appVersionCode, activity)) {
                    if (dialog.conditions?.showOnce == true) {
                        markAsShown(activity, dialog.id)
                    }
                    withContext(Dispatchers.Main) {
                        showDialog(activity, dialog)
                    }
                    return
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch or show dialogs", e)
        }
    }

    private fun shouldShow(dialog: DialogDef, appVersionCode: Long, context: Context): Boolean {
        val conditions = dialog.conditions ?: return true

        if (conditions.showOnce && wasShown(context, dialog.id)) return false
        if (conditions.minVersionCode != null && appVersionCode < conditions.minVersionCode) return false
        if (conditions.maxVersionCode != null && appVersionCode > conditions.maxVersionCode) return false

        val today = LocalDate.now()
        if (conditions.startDate != null) {
            val start = try { LocalDate.parse(conditions.startDate) } catch (e: DateTimeParseException) { null }
            if (start == null || today.isBefore(start)) return false
        }
        if (conditions.endDate != null) {
            val end = try { LocalDate.parse(conditions.endDate) } catch (e: DateTimeParseException) { null }
            if (end == null || today.isAfter(end)) return false
        }

        return true
    }

    private fun showDialog(activity: FragmentActivity, dialog: DialogDef) {
        val fragment = RemoteDialogFragment.newInstance(dialog)
        fragment.show(activity.supportFragmentManager, "remote_dialog_${dialog.id}")
    }

    private fun getAppVersionCode(context: Context): Long {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(info)
        } catch (e: Exception) {
            0L
        }
    }

    private fun wasShown(context: Context, dialogId: String): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("shown_$dialogId", false)
    }

    private fun markAsShown(context: Context, dialogId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("shown_$dialogId", true)
            .apply()
    }
}
