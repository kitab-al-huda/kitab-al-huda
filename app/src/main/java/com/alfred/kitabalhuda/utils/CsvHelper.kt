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
                // Load Reciteurs
                val reciteurs = loadReciteurs(context)
                database.reciteurDao().insertAll(reciteurs)

                // Load Sourates
                val sourates = loadSourates(context)
                database.sourateDao().insertAll(sourates)

                // Load Audios
                val audios = loadAudios(context)
                database.audioDao().insertAll(audios)

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
                            val sourateNumero = tokens[1].trim().toInt()
                            val duree = tokens[2].trim().toLong()
                            val urlWeb = tokens[3].trim()
                            val pathLocal = if (tokens.size > 4 && tokens[4].trim().isNotEmpty()) tokens[4].trim() else null
                            list.add(AudioEntity(reciteurId = reciteurId, sourateNumero = sourateNumero, duree = duree, urlWeb = urlWeb, pathLocal = pathLocal))
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
