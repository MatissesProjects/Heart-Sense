package com.heart.sense.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_intakes")
data class MedicationIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medId: Int,
    val medName: String,
    val timestamp: Long,
    val dose: String,
    val source: String, // "Phone", "Watch"
    val status: String = "Taken" // "Taken", "Missed", "Skipped"
)
