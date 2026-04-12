package com.heart.sense.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_glucose_records")
data class BloodGlucose(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val value: Double, // mmol/L or mg/dL - we'll standardize to mmol/L internally
    val unit: String = "mmol/L"
)
