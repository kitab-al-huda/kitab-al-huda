package com.alfred.kitabalhuda.network

import com.google.gson.annotations.SerializedName

/**
 * Modèles GSON pour parser la réponse de l'API Facebook Graph
 * GET /v24.0/{message_id}?fields=attachments
 *
 * Structure attendue :
 * {
 *   "attachments": {
 *     "data": [{
 *       "mime_type": "audio/mpeg",
 *       "size": 12345,
 *       "video_data": {
 *         "url": "https://scontent.xx.fbcdn.net/..."
 *       }
 *     }]
 *   }
 * }
 */
data class AttachmentResponse(
    @SerializedName("attachments")
    val attachments: AttachmentWrapper?
)

data class AttachmentWrapper(
    @SerializedName("data")
    val data: List<AttachmentData>?
)

data class AttachmentData(
    @SerializedName("mime_type")
    val mimeType: String?,

    @SerializedName("size")
    val size: Long?,

    @SerializedName("video_data")
    val videoData: VideoData?,

    @SerializedName("file_url")
    val fileUrl: String?
)

data class VideoData(
    @SerializedName("url")
    val url: String?
)
