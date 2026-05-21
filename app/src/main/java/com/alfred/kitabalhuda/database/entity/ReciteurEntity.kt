package com.alfred.kitabalhuda.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "reciteurs")
data class ReciteurEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,

    @SerializedName("nom")
    val nom: String,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("description")
    val description: String?
)
