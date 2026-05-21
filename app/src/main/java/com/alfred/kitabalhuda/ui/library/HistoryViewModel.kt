package com.alfred.kitabalhuda.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.database.dao.ListeningHistoryDao

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val historyDao: ListeningHistoryDao
    
    init {
        val database = (application as KitabAlHudaApplication).database
        historyDao = database.listeningHistoryDao()
    }
    
    val recentHistory: LiveData<List<ListeningHistoryDao.HistoryItem>> = historyDao.getRecentHistory(50)
}
