package com.alfred.kitabalhuda.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "sourates")
data class SourateEntity(
    @PrimaryKey
    @SerializedName("numero")
    val numero: Int,

    @SerializedName("nomArabe")
    val nomArabe: String,

    @SerializedName("nomPhonetique")
    val nomPhonetique: String,

    @SerializedName("nombreVersets")
    val nombreVersets: Int,

    @SerializedName("lieuRevelation")
    val lieuRevelation: String
)
