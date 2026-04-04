package com.heart.sense.data

data class Settings(
    val highHrThreshold: Int = 100,
    val isSickMode: Boolean = false,
    val lastUpdated: Long = 0L,
    val snoozeUntil: Long = 0L
) {
    val isSnoozed: Boolean
        get() = System.currentTimeMillis() < snoozeUntil

    val snoozeRemainingMinutes: Long
        get() = if (isSnoozed) (snoozeUntil - System.currentTimeMillis()) / 60000 else 0L
}
