package com.heart.sense.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey val visitId: String,
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime? = null,
    val clinicianNotes: String? = null
)
