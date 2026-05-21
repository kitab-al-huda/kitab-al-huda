package com.alfred.kitabalhuda.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.alfred.kitabalhuda.database.entity.HadithEntity

@Dao
interface HadithDao {
    @Query("SELECT * FROM hadiths LIMIT 1")
    suspend fun getRandomHadith(): HadithEntity?
}
