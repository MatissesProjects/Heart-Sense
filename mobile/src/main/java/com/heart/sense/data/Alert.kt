package com.heart.sense.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hr: Int,
    val type: String, // "High HR" or "Sit Down"
    val timestamp: LocalDateTime = LocalDateTime.now()
)
