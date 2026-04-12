package com.heart.sense.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "ble_sensor_data")
data class BleSensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val deviceName: String,
    val deviceAddress: String,
    val heartRate: Int,
    val rrIntervals: String? = null, // List of ms
    val batteryLevel: Int? = null
)
