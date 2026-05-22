package com.alfred.kitabalhuda.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit pour l'API Facebook Graph.
 * Utilisée pour récupérer l'URL CDN temporaire d'un audio attaché à un message Messenger.
 *
 * L'URL de base est déchiffrée dynamiquement depuis BuildConfig.API_ENDPOINT_BASE
 * via CryptoUtils et passée au Retrofit.Builder dans MessengerRepository.
 */
interface FacebookApiService {

    /**
     * Récupère les pièces jointes d'un message Messenger.
     * L'audio sera dans attachments.data[0].video_data.url ou attachments.data[0].file_url
     *
     * @param messageId L'ID du message Messenger (ex: "m_IYQwk4Vv0h...")
     * @param fields Champs à récupérer (default: "attachments")
     * @param accessToken Le token d'accès à la page Facebook
     */
    @GET("v24.0/{messageId}")
    suspend fun getMessageAttachments(
        @Path("messageId") messageId: String,
        @Query("fields") fields: String = "attachments",
        @Query("access_token") accessToken: String
    ): AttachmentResponse
}
