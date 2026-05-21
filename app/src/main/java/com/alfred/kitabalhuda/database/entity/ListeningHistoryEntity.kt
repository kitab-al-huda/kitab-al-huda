package com.alfred.kitabalhuda.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "listening_history",
    foreignKeys = [
        ForeignKey(
            entity = AudioEntity::class,
            parentColumns = ["id"],
            childColumns = ["audioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("audioId"), Index("timestamp")]
)
data class ListeningHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audioId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
