package com.heart.sense.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dose: String,
    val frequency: String, // e.g., "Daily", "Weekly"
    val reminderTime: String, // e.g., "08:00"
    val isActive: Boolean = true,
    val lastSyncTimestamp: Long = 0
)
