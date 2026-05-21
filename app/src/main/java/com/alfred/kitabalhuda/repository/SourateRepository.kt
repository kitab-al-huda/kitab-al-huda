package com.alfred.kitabalhuda.repository

import com.alfred.kitabalhuda.database.dao.SourateDao
import com.alfred.kitabalhuda.database.entity.SourateEntity
import kotlinx.coroutines.flow.Flow

class SourateRepository(private val sourateDao: SourateDao) {

    fun getAllSourates(): Flow<List<SourateEntity>> {
        return sourateDao.getAllSourates()
    }
}
