package com.alfred.kitabalhuda.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Hadith(
    val Book: String,
    val Chapter_Number: Int,
    val Chapter_Title_Arabic: String,
    val Chapter_Title_English: String,
    val Arabic_Text: String,
    val English_Text: String,
    val Grade: String,
    val Reference: String
)

object HadithManager {

    suspend fun getRandomHadith(context: Context): Hadith? = withContext(Dispatchers.IO) {
        try {
            context.assets.open("json/sahih_muslim.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<List<Hadith>>() {}.type
                    val hadiths: List<Hadith> = Gson().fromJson(reader, type)
                    if (hadiths.isNotEmpty()) {
                        hadiths.random()
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HadithManager", "Failed to load hadith", e)
            null
        }
    }
}

