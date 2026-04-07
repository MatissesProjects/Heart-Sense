package com.heart.sense.wear.util

import androidx.health.services.client.data.UserActivityState
import java.util.*

object PacingDetector {
    private const val WINDOW_SIZE = 10 // Last 10 activity updates
    private val activityHistory = LinkedList<UserActivityState>()
    private var lastAlertTime = 0L

    fun process(state: UserActivityState, hr: Int, restingHr: Int): Boolean {
        activityHistory.add(state)
        if (activityHistory.size > WINDOW_SIZE) {
            activityHistory.removeFirst()
        }

        if (System.currentTimeMillis() - lastAlertTime < 60000) return false // Cool down 1 min

        // Pacing heuristic: Multiple transitions between PASSIVE and WALKING 
        // while heart rate is significantly above resting.
        val transitions = countTransitions()
        val isHrElevated = hr > restingHr + 15

        if (transitions >= 3 && isHrElevated) {
            lastAlertTime = System.currentTimeMillis()
            return true
        }

        return false
    }

    private fun countTransitions(): Int {
        if (activityHistory.size < 4) return 0
        var count = 0
        for (i in 1 until activityHistory.size) {
            if (activityHistory[i] != activityHistory[i-1]) {
                count++
            }
        }
        return count
    }
}
