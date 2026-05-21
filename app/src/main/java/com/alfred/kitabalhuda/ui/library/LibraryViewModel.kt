package com.alfred.kitabalhuda.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.repository.PlaylistRepository
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlaylistRepository

    val allPlaylists: LiveData<List<PlaylistEntity>>

    init {
        val database = (application as KitabAlHudaApplication).database
        // TODO: We should use dependency injection properly, but for now manual injection
        repository = PlaylistRepository(database.playlistDao())
        allPlaylists = repository.allPlaylists
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }
    
    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun updatePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.updatePlaylist(playlist)
        }
    }

    fun addTrackToPlaylist(playlistId: Int, audioId: Long) {
        viewModelScope.launch {
             repository.addAudioToPlaylist(playlistId, audioId)
        }
    }
}