package com.alfred.kitabalhuda.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "audios",
    foreignKeys = [
        ForeignKey(
            entity = ReciteurEntity::class,
            parentColumns = ["id"],
            childColumns = ["reciteurId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SourateEntity::class,
            parentColumns = ["numero"],
            childColumns = ["sourateNumero"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reciteurId"), Index("sourateNumero")]
)
data class AudioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @SerializedName("reciteurId")
    val reciteurId: Int,

    @SerializedName("sourateNumero")
    val sourateNumero: Int,

    @SerializedName("duree")
    val duree: Long,

    @SerializedName("urlWeb")
    val urlWeb: String,

    @SerializedName("pathLocal")
    val pathLocal: String? = null
)
