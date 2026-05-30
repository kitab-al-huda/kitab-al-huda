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

    fun createPlaylistWithTracks(name: String, audioIds: List<Long>) {
        viewModelScope.launch {
            val playlistId = repository.createPlaylist(name)
            repository.addAudioPartsToPlaylist(playlistId.toInt(), audioIds)
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

    /**
     * Adds ALL parts of a surah to a playlist.
     * For single-part surahs: audioIds has 1 element.
     * For multi-part surahs (e.g. Al-Baqara with 5 Minshawi parts): all 5 are inserted in order.
     */
    fun addTracksToPlaylist(playlistId: Int, audioIds: List<Long>) {
        viewModelScope.launch {
            repository.addAudioPartsToPlaylist(playlistId, audioIds)
        }
    }
}