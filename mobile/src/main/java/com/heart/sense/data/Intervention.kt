package com.heart.sense.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "interventions")
data class Intervention(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val type: String, // e.g., "Box Breathing", "Sensory Break", "Walking"
    val trigger: String?, // e.g., "Noise", "Transition"
    
    // Physiological state BEFORE intervention
    val startHr: Int,
    val startHrv: Float,
    
    // Physiological state AFTER intervention
    val endHr: Int?,
    val endHrv: Float?,
    
    // Calculated efficiency (The "Reward" for RL)
    val recoveryScore: Float = 0f,
    val visitId: String? = null
)
