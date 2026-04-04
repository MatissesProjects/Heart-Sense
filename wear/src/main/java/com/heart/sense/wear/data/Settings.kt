package com.heart.sense.wear.data

data class Settings(
    val highHrThreshold: Int = 100,
    val isSickMode: Boolean = false
) {
    val effectiveThreshold: Int
        get() = if (isSickMode) highHrThreshold - 10 else highHrThreshold
}
