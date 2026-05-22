package com.alfred.kitabalhuda.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.database.entity.PlaylistItemEntity
import com.alfred.kitabalhuda.database.entity.SourateEntity
import com.alfred.kitabalhuda.database.entity.ReciteurEntity

@Dao
interface PlaylistDao {
    
    // --- Playlist Operations ---
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): LiveData<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    // --- Playlist Item Operations ---
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getPlaylistItems(playlistId: Int): LiveData<List<PlaylistItemEntity>>

    @Query("SELECT MAX(orderIndex) FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun getMaxOrderForPlaylist(playlistId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlaylistItem(item: PlaylistItemEntity)

    @Delete
    suspend fun removePlaylistItem(item: PlaylistItemEntity)
    
    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Int)

    @Update
    suspend fun updatePlaylistItem(item: PlaylistItemEntity)
    
    // Helper to get playlist info with full track details (Join with Sourate and Reciteur)
    @Transaction
    @Query("""
        SELECT 
            playlist_items.id AS item_id,
            playlist_items.playlistId AS item_playlistId, 
            playlist_items.audioId AS item_audioId, 
            playlist_items.orderIndex AS item_orderIndex,
            audios.id AS audio_id, 
            audios.reciteurId AS audio_reciteurId, 
            audios.sourateNumero AS audio_sourateNumero, 
            audios.duree AS audio_duree, 
            audios.urlWeb AS audio_urlWeb, 
            audios.pathLocal AS audio_pathLocal,
            audios.fbMessageId AS audio_fbMessageId,
            audios.partNumber AS audio_partNumber,
            sourates.numero AS sourate_numero,
            sourates.nomArabe AS sourate_nomArabe,
            sourates.nomPhonetique AS sourate_nomPhonetique,
            sourates.lieuRevelation AS sourate_lieuRevelation,
            sourates.nombreVersets AS sourate_nombreVersets,
            reciteurs.id AS reciteur_id,
            reciteurs.nom AS reciteur_nom,
            reciteurs.imageUrl AS reciteur_imageUrl,
            reciteurs.description AS reciteur_description
        FROM playlist_items 
        INNER JOIN audios ON playlist_items.audioId = audios.id 
        INNER JOIN sourates ON audios.sourateNumero = sourates.numero
        INNER JOIN reciteurs ON audios.reciteurId = reciteurs.id
        WHERE playlistId = :playlistId 
        ORDER BY orderIndex ASC
    """)
    fun getPlaylistTracks(playlistId: Int): LiveData<List<PlaylistTrack>>
    
    data class PlaylistTrack(
        @Embedded(prefix = "item_") val item: PlaylistItemEntity,
        @Embedded(prefix = "audio_") val audio: AudioEntity,
        @Embedded(prefix = "sourate_") val sourate: SourateEntity,
        @Embedded(prefix = "reciteur_") val reciteur: ReciteurEntity
    )
}
