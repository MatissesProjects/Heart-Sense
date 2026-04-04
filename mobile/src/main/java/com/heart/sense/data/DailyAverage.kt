package com.heart.sense.data

import java.time.LocalDate

data class DailyAverage(
    val date: LocalDate,
    val avgHr: Int,
    val avgRr: Float,
    val sampleCount: Int,
    val isAlertTriggered: Boolean,
    val alertType: String? = null
)
