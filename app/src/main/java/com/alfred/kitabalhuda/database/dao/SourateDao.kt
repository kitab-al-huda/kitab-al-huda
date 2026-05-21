package com.alfred.kitabalhuda.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alfred.kitabalhuda.database.entity.SourateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourateDao {
    @Query("SELECT * FROM sourates ORDER BY numero ASC")
    fun getAllSourates(): Flow<List<SourateEntity>>

    @Query("SELECT * FROM sourates ORDER BY numero ASC")
    suspend fun getAllSouratesDirect(): List<SourateEntity>

    @Query("SELECT * FROM sourates WHERE numero = :id")
    suspend fun getSourateById(id: Int): SourateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sourates: List<SourateEntity>)
}
