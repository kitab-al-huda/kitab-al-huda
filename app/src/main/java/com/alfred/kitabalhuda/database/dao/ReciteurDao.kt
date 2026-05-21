package com.alfred.kitabalhuda.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReciteurDao {
    @Query("SELECT * FROM reciteurs ORDER BY nom ASC")
    fun getAllReciteurs(): Flow<List<ReciteurEntity>>

    @Query("SELECT * FROM reciteurs WHERE id = :id")
    suspend fun getReciteurById(id: Int): ReciteurEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reciteurs: List<ReciteurEntity>)
}
