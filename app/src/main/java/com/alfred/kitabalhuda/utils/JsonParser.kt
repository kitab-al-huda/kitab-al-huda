package com.alfred.kitabalhuda.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object JsonParser {
    // Rendre public pour pouvoir utiliser inline avec reified
    val gson = Gson()
    
    /**
     * Essaye de parser une chaîne JSON en un objet du type spécifié.
     * Retourne null en cas d'erreur.
     */
    inline fun <reified T> tryParseJson(json: String): T? {
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    }
} 