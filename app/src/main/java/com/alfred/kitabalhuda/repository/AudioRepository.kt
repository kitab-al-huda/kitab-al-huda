package com.alfred.kitabalhuda.repository

import com.alfred.kitabalhuda.database.dao.AudioDao
import com.alfred.kitabalhuda.database.entity.AudioEntity
import kotlinx.coroutines.flow.Flow

class AudioRepository(private val audioDao: AudioDao) {

    suspend fun getAudioForSurahAndReciter(reciteurId: Int, sourateNumber: Int): AudioEntity? {
        return audioDao.getAudioForSurahAndReciter(reciteurId, sourateNumber)
    }

    fun getAudiosByReciteur(reciteurId: Int): Flow<List<AudioEntity>> {
        return audioDao.getAudiosByReciteur(reciteurId)
    }
}
