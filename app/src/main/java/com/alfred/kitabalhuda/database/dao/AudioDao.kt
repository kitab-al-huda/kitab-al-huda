package com.alfred.kitabalhuda.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alfred.kitabalhuda.database.entity.AudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {
    @Query("SELECT * FROM audios WHERE reciteurId = :reciteurId ORDER BY sourateNumero ASC, partNumber ASC")
    fun getAudiosByReciteur(reciteurId: Int): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audios WHERE reciteurId = :reciteurId ORDER BY sourateNumero ASC, partNumber ASC")
    suspend fun getAudiosByReciteurDirect(reciteurId: Int): List<AudioEntity>

    @Query("SELECT * FROM audios WHERE sourateNumero = :sourateId")
    fun getAudiosBySourate(sourateId: Int): Flow<List<AudioEntity>>

    @Query("SELECT * FROM audios WHERE reciteurId = :reciteurId AND sourateNumero = :sourateNumber LIMIT 1")
    suspend fun getAudioForSurahAndReciter(reciteurId: Int, sourateNumber: Int): AudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(audios: List<AudioEntity>)

    @Query("SELECT * FROM audios WHERE reciteurId = :reciteurId AND sourateNumero = :sourateNumber ORDER BY partNumber ASC")
    suspend fun getAudioPartsForSurah(reciteurId: Int, sourateNumber: Int): List<AudioEntity>
}
