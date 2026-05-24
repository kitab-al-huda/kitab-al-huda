package com.alfred.kitabalhuda

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alfred.kitabalhuda.database.AppDatabase
import com.alfred.kitabalhuda.di.ViewModelFactory
import com.alfred.kitabalhuda.utils.PreferenceManager
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.alfred.kitabalhuda.utils.CsvHelper

class KitabAlHudaApplication : Application(), Configuration.Provider {

    // internal lateinit var viewModelFactory: ViewModelFactory
    
    // Base de données
    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        
        // Force Arabic Locale
        val locale = Locale("ar")
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)

        // Populate Database with CSV if empty
        CoroutineScope(Dispatchers.IO).launch {
             CsvHelper.populateDatabase(this@KitabAlHudaApplication, database)
        }
    }

    // Implémentation de Configuration.Provider pour initialiser WorkManager
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}