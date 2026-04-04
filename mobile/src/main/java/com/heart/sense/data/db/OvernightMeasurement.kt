package com.heart.sense.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "overnight_measurements")
data class OvernightMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val heartRate: Int,
    val respiratoryRate: Float?,
    val activityState: Int
)
