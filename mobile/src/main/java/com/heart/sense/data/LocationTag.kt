package com.heart.sense.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_tags")
data class LocationTag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val alertType: String,
    val intensity: Int, // HR or Stress score
    val visitId: String? = null
)
