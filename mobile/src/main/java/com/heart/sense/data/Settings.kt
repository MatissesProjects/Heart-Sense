package com.heart.sense.data

data class Settings(
    val highHrThreshold: Int = 100,
    val isSickMode: Boolean = false,
    val lastUpdated: Long = 0L
)
