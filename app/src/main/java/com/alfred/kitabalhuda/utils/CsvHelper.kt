package com.alfred.kitabalhuda.utils

import android.content.Context
import com.alfred.kitabalhuda.database.AppDatabase
import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import com.alfred.kitabalhuda.database.entity.SourateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvHelper {

    suspend fun populateDatabase(context: Context, database: AppDatabase) {
        withContext(Dispatchers.IO) {
            try {
                // Check if already populated to prevent cascade deletes of user playlists/history on restart
                if (database.sourateDao().getAllSouratesDirect().isNotEmpty()) {
                    return@withContext
                }

                // Load Reciteurs
                val reciteurs = loadReciteurs(context)
                database.reciteurDao().insertAll(reciteurs)

                // Load Sourates
                val sourates = loadSourates(context)
                database.sourateDao().insertAll(sourates)

                // Load Audios (reciteurs classiques via HTTP direct)
                val audios = loadAudios(context)
                database.audioDao().insertAll(audios)

                // Load Minshawi Audios (Messenger BDD — zero-rated)
                val minshawiAudios = loadMinshawiAudios(context)
                database.audioDao().insertAll(minshawiAudios)

                // Load Afasy Audios (Messenger BDD — zero-rated)
                val afasyAudios = loadAfasyAudios(context)
                database.audioDao().insertAll(afasyAudios)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadReciteurs(context: Context): List<ReciteurEntity> {
        val list = mutableListOf<ReciteurEntity>()
        context.assets.open("csv/reciteurs.csv").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine() // Skip header
                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",") // Simple split, assumes no commas in fields for MVP
                    if (tokens.size >= 3) {
                        try {
                            val id = tokens[0].trim().toInt()
                            val nom = tokens[1].trim()
                            val imageUrl = tokens[2].trim()
                            val description = if (tokens.size > 3) tokens[3].trim() else null
                            list.add(ReciteurEntity(id, nom, imageUrl, description))
                        } catch (e: Exception) {
                             // Log or ignore malformed line
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return list
    }

    private fun loadSourates(context: Context): List<SourateEntity> {
        val list = mutableListOf<SourateEntity>()
        context.assets.open("csv/sourates.csv").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine() // Skip header
                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",")
                    if (tokens.size >= 5) {
                        try {
                            val numero = tokens[0].trim().toInt()
                            val nomArabe = tokens[1].trim()
                            val nomPhonetique = tokens[2].trim()
                            val nombreVersets = tokens[3].trim().toInt()
                            val lieuRevelation = tokens[4].trim()
                            list.add(SourateEntity(numero, nomArabe, nomPhonetique, nombreVersets, lieuRevelation))
                        } catch (e: Exception) {
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return list
    }

    private fun loadAudios(context: Context): List<AudioEntity> {
        val list = mutableListOf<AudioEntity>()
        context.assets.open("csv/audios.csv").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine() // Skip header
                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",")
                    if (tokens.size >= 4) {
                        try {
                            val reciteurId = tokens[0].trim().toInt()
                            // Exclure les réciteurs ayant des fichiers audio zero-rated dédiés
                            if (reciteurId != 1 && reciteurId != 6) {
                                val sourateNumero = tokens[1].trim().toInt()
                                val duree = tokens[2].trim().toLong()
                                val urlWeb = tokens[3].trim()
                                val pathLocal = if (tokens.size > 4 && tokens[4].trim().isNotEmpty()) tokens[4].trim() else null
                                list.add(AudioEntity(reciteurId = reciteurId, sourateNumero = sourateNumero, duree = duree, urlWeb = urlWeb, pathLocal = pathLocal))
                            }
                        } catch (e: Exception) {
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return list
    }

    private fun loadMinshawiAudios(context: Context): List<AudioEntity> {
        val list = mutableListOf<AudioEntity>()
        context.assets.open("csv/zero_rated/audios_minshawi.csv").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine() // Skip header
                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",")
                    if (tokens.size >= 7) {
                        try {
                            val reciteurId = tokens[0].trim().toInt()
                            val sourateNumero = tokens[1].trim().toInt()
                            val duree = tokens[2].trim().toLong()
                            val urlWeb = tokens[3].trim()
                            val pathLocal = if (tokens[4].trim().isNotEmpty()) tokens[4].trim() else null
                            val fbMessageId = if (tokens[5].trim().isNotEmpty()) tokens[5].trim() else null
                            val partNumber = tokens[6].trim().toInt()
                            list.add(
                                AudioEntity(
                                    reciteurId = reciteurId,
                                    sourateNumero = sourateNumero,
                                    duree = duree,
                                    urlWeb = urlWeb,
                                    pathLocal = pathLocal,
                                    fbMessageId = fbMessageId,
                                    partNumber = partNumber
                                )
                            )
                        } catch (e: Exception) {
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return list
    }

    private fun loadAfasyAudios(context: Context): List<AudioEntity> {
        val list = mutableListOf<AudioEntity>()
        context.assets.open("csv/zero_rated/audios_afasy.csv").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readLine() // Skip header
                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",")
                    if (tokens.size >= 7) {
                        try {
                            val reciteurId = tokens[0].trim().toInt()
                            val sourateNumero = tokens[1].trim().toInt()
                            val duree = tokens[2].trim().toLong()
                            val urlWeb = tokens[3].trim()
                            val pathLocal = if (tokens[4].trim().isNotEmpty()) tokens[4].trim() else null
                            val fbMessageId = if (tokens[5].trim().isNotEmpty()) tokens[5].trim() else null
                            val partNumber = tokens[6].trim().toInt()
                            list.add(
                                AudioEntity(
                                    reciteurId = reciteurId,
                                    sourateNumero = sourateNumero,
                                    duree = duree,
                                    urlWeb = urlWeb,
                                    pathLocal = pathLocal,
                                    fbMessageId = fbMessageId,
                                    partNumber = partNumber
                                )
                            )
                        } catch (e: Exception) {
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return list
    }
}
