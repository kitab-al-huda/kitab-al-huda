package com.alfred.kitabalhuda.repository

import androidx.lifecycle.LiveData
import com.alfred.kitabalhuda.database.dao.PlaylistDao
import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.database.entity.PlaylistItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepository(private val playlistDao: PlaylistDao) {

    val allPlaylists: LiveData<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val allPlaylistsWithCount: LiveData<List<PlaylistDao.PlaylistWithCount>> = playlistDao.getAllPlaylistsWithCount()

    suspend fun createPlaylist(name: String, description: String? = null): Long {
        return withContext(Dispatchers.IO) {
            val playlist = PlaylistEntity(name = name, description = description)
            playlistDao.createPlaylist(playlist)
        }
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        withContext(Dispatchers.IO) {
            playlistDao.deletePlaylist(playlist)
        }
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity) {
        withContext(Dispatchers.IO) {
            playlistDao.updatePlaylist(playlist)
        }
    }

    fun getPlaylistTracks(playlistId: Int): LiveData<List<PlaylistDao.PlaylistTrack>> {
        return playlistDao.getPlaylistTracks(playlistId)
    }

    suspend fun addAudioToPlaylist(playlistId: Int, audioId: Long) {
        addAudioPartsToPlaylist(playlistId, listOf(audioId))
    }

    /**
     * Inserts all parts of a surah into a playlist in order.
     * Calling this with a single-element list works the same as addAudioToPlaylist.
     */
    suspend fun addAudioPartsToPlaylist(playlistId: Int, audioIds: List<Long>) {
        withContext(Dispatchers.IO) {
            val maxOrder = playlistDao.getMaxOrderForPlaylist(playlistId) ?: -1
            audioIds.forEachIndexed { index, audioId ->
                playlistDao.addPlaylistItem(
                    PlaylistItemEntity(
                        playlistId = playlistId,
                        audioId = audioId,
                        orderIndex = maxOrder + 1 + index
                    )
                )
            }
        }
    }

    suspend fun removeTrackFromPlaylist(item: PlaylistItemEntity) {
        withContext(Dispatchers.IO) {
            playlistDao.removePlaylistItem(item)
        }
    }
    
    suspend fun updateTrackOrder(items: List<PlaylistItemEntity>) {
         withContext(Dispatchers.IO) {
             items.forEach { playlistDao.updatePlaylistItem(item = it) }
         }
    }
}
