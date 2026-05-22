package com.alfred.kitabalhuda.repository

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.alfred.kitabalhuda.BuildConfig
import com.alfred.kitabalhuda.network.FacebookApiService
import com.alfred.kitabalhuda.utils.CryptoUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository pour résoudre les message IDs Messenger en URLs CDN temporaires.
 * 
 * Utilise un LruCache pour éviter des appels API redondants.
 * Les URLs CDN Facebook expirent après ~1h, donc le cache est dimensionné
 * pour une session de lecture typique.
 */
class MessengerRepository private constructor(context: Context) {

    companion object {
        private const val TAG = "MessengerRepository"
        private const val CACHE_SIZE = 120 // ~114 surahs + multi-parts

        @Volatile
        private var INSTANCE: MessengerRepository? = null

        fun getInstance(context: Context): MessengerRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MessengerRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val accessToken: String
    private val apiService: FacebookApiService
    private val urlCache = LruCache<String, CachedUrl>(CACHE_SIZE)

    init {
        val appContext = context.applicationContext

        // Déchiffrer les credentials
        val baseUrl = CryptoUtils.decrypt(BuildConfig.API_ENDPOINT_BASE, appContext)
        accessToken = CryptoUtils.decrypt(BuildConfig.API_AUTH_SIGNATURE, appContext)

        // Créer le client Retrofit avec l'URL de base déchiffrée
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(FacebookApiService::class.java)
    }

    /**
     * Résout un message ID Messenger en URL CDN temporaire.
     * Utilise le cache si l'URL n'a pas expiré (~45 min de TTL).
     *
     * @param messageId L'ID du message Messenger (ex: "m_IYQwk4Vv0h...")
     * @return L'URL CDN directe pour le streaming, ou null en cas d'erreur
     */
    suspend fun resolveAudioUrl(messageId: String): String? {
        // Vérifier le cache
        val cached = urlCache.get(messageId)
        if (cached != null && !cached.isExpired()) {
            Log.d(TAG, "Cache hit for $messageId")
            return cached.url
        }

        return try {
            Log.d(TAG, "Resolving CDN URL for message: $messageId")
            val response = apiService.getMessageAttachments(
                messageId = messageId,
                accessToken = accessToken
            )

            // Extraire l'URL CDN depuis la réponse
            val attachmentData = response.attachments?.data?.firstOrNull()
            val cdnUrl = attachmentData?.videoData?.url
                ?: attachmentData?.fileUrl

            if (cdnUrl != null) {
                urlCache.put(messageId, CachedUrl(cdnUrl))
                Log.d(TAG, "Resolved $messageId → ${cdnUrl.take(80)}...")
            } else {
                Log.w(TAG, "No audio URL found in attachment for $messageId")
            }

            cdnUrl
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve URL for $messageId", e)
            null
        }
    }

    /**
     * URL mise en cache avec timestamp pour gérer l'expiration.
     * Les URLs CDN Facebook expirent typiquement après ~1h.
     */
    private data class CachedUrl(
        val url: String,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean {
            val ttlMillis = 45 * 60 * 1000L // 45 minutes (marge de sécurité vs 1h)
            return System.currentTimeMillis() - timestamp > ttlMillis
        }
    }
}
