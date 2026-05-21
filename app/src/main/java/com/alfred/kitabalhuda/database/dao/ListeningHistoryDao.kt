package com.alfred.kitabalhuda.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import com.alfred.kitabalhuda.database.entity.SourateEntity

@Dao
interface ListeningHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToHistory(history: ListeningHistoryEntity)
    
    @Query("DELETE FROM listening_history")
    suspend fun clearHistory()
    
    @Query("DELETE FROM listening_history WHERE id = :id")
    suspend fun removeFromHistory(id: Long)
    
    // Get history with full details (join with Audio, Sourate, Reciteur)
    @Transaction
    @Query("""
        SELECT DISTINCT
            listening_history.id AS history_id,
            listening_history.audioId AS history_audioId,
            listening_history.timestamp AS history_timestamp,
            audios.id AS audio_id, 
            audios.reciteurId AS audio_reciteurId, 
            audios.sourateNumero AS audio_sourateNumero, 
            audios.duree AS audio_duree, 
            audios.urlWeb AS audio_urlWeb, 
            audios.pathLocal AS audio_pathLocal,
            sourates.numero AS sourate_numero,
            sourates.nomArabe AS sourate_nomArabe,
            sourates.nomPhonetique AS sourate_nomPhonetique,
            sourates.lieuRevelation AS sourate_lieuRevelation,
            sourates.nombreVersets AS sourate_nombreVersets,
            reciteurs.id AS reciteur_id,
            reciteurs.nom AS reciteur_nom,
            reciteurs.imageUrl AS reciteur_imageUrl,
            reciteurs.description AS reciteur_description
        FROM listening_history
        INNER JOIN audios ON listening_history.audioId = audios.id 
        INNER JOIN sourates ON audios.sourateNumero = sourates.numero
        INNER JOIN reciteurs ON audios.reciteurId = reciteurs.id
        ORDER BY listening_history.timestamp DESC
        LIMIT :limit
    """)
    fun getRecentHistory(limit: Int = 50): LiveData<List<HistoryItem>>
    
    data class HistoryItem(
        @Embedded(prefix = "history_") val history: ListeningHistoryEntity,
        @Embedded(prefix = "audio_") val audio: AudioEntity,
        @Embedded(prefix = "sourate_") val sourate: SourateEntity,
        @Embedded(prefix = "reciteur_") val reciteur: ReciteurEntity
    )
}
