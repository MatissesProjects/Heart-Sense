package com.heart.sense.data

import java.time.LocalDate

data class DailyAverage(
    val date: LocalDate,
    val avgHr: Int,
    val avgRr: Float,
    val sampleCount: Int,
    val isAlertTriggered: Boolean,
    val alertType: String? = null,
    val hrvRmssd: Float = 0f,
    val avgAmbientTemp: Float? = null,
    val avgAmbientLux: Float? = null,
    val avgAmbientDb: Int? = null,
    val avgSpo2: Float? = null
)
