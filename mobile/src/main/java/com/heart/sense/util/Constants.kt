package com.heart.sense.util

object Constants {
    // Wearable Data Layer Paths
    const val PATH_SETTINGS = "/settings"
    const val PATH_HR_ALERT = "/hr_alert"
    const val PATH_CRITICAL_HR = "/critical_hr"
    const val PATH_SIT_DOWN = "/sit_down"
    const val PATH_LIVE_HR = "/live_hr"
    const val PATH_STOP_HMS = "/stop_hms"

    // Data Map Keys
    const val KEY_HIGH_HR_THRESHOLD = "highHrThreshold"
    const val KEY_IS_SICK_MODE = "isSickMode"
    const val KEY_LAST_UPDATED = "lastUpdated"
    const val KEY_SNOOZE_UNTIL = "snoozeUntil"

    // Intent Actions
    const val ACTION_SICK_MODE = "com.heart.sense.ACTION_SICK_MODE"
    const val ACTION_ACKNOWLEDGE = "com.heart.sense.ACTION_ACKNOWLEDGE"
    const val ACTION_SNOOZE = "com.heart.sense.ACTION_SNOOZE"
    const val ACTION_FALSE_POSITIVE_EXERCISE = "com.heart.sense.ACTION_FALSE_POSITIVE_EXERCISE"
    const val ACTION_EMERGENCY_CONTACT = "com.heart.sense.ACTION_EMERGENCY_CONTACT"
}
