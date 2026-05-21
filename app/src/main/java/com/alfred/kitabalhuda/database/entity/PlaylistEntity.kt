package com.alfred.kitabalhuda.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val coverUrl: String? = null, // Can be local path or URL
    val createdAt: Long = System.currentTimeMillis()
)
