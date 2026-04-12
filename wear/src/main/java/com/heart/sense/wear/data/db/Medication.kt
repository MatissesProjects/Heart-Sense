package com.heart.sense.wear.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey val id: Int,
    val name: String,
    val dose: String,
    val frequency: String,
    val reminderTime: String
)
