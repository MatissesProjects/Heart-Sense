package com.heart.sense.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "environmental_context")
data class EnvironmentalContext(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val aqi: Int,
    val temperature: Float,
    val humidity: Int,
    val pressure: Float,
    val description: String? = null
)
