package com.alfred.kitabalhuda.repository

import com.alfred.kitabalhuda.database.dao.ReciteurDao
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import kotlinx.coroutines.flow.Flow

class ReciteurRepository(private val reciteurDao: ReciteurDao) {

    fun getAllReciteurs(): Flow<List<ReciteurEntity>> {
        return reciteurDao.getAllReciteurs()
    }
    
    suspend fun getReciteurById(id: Int): ReciteurEntity? {
        return reciteurDao.getReciteurById(id)
    }
}
