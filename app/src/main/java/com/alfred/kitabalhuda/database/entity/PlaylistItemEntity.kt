package com.alfred.kitabalhuda.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlist_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AudioEntity::class,
            parentColumns = ["id"],
            childColumns = ["audioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["audioId"])
    ]
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Int,
    val audioId: Long,
    val orderIndex: Int // For reordering
)
