package com.heart.sense.data

import java.time.LocalDateTime

data class Alert(
    val hr: Int,
    val type: String, // "High HR" or "Sit Down"
    val timestamp: LocalDateTime = LocalDateTime.now()
)
