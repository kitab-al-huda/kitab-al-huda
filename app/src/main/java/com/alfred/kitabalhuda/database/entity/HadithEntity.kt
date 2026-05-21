package com.alfred.kitabalhuda.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hadiths")
data class HadithEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val book: String,
    val chapter: String
)
