package com.alfred.kitabalhuda.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.database.dao.PlaylistDao
import com.alfred.kitabalhuda.database.entity.PlaylistItemEntity
import com.alfred.kitabalhuda.repository.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlaylistRepository

    init {
        val database = (application as KitabAlHudaApplication).database
        repository = PlaylistRepository(database.playlistDao())
    }

    fun getPlaylistTracks(playlistId: Int): LiveData<List<PlaylistDao.PlaylistTrack>> {
        return repository.getPlaylistTracks(playlistId)
    }

    fun removeTrack(item: PlaylistItemEntity) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(item)
        }
    }

    fun updateOrder(items: List<PlaylistDao.PlaylistTrack>) {
        viewModelScope.launch {
            // Update each item's orderIndex based on the list position
            val updates = items.mapIndexed { index, track ->
                track.item.copy(orderIndex = index)
            }
            repository.updateTrackOrder(updates)
        }
    }
}
