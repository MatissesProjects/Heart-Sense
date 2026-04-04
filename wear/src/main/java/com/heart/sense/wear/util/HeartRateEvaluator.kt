package com.heart.sense.wear.util

import androidx.health.services.client.data.UserActivityState
import com.heart.sense.wear.data.Settings

sealed class MonitoringAction {
    object None : MonitoringAction()
    data class TriggerAlert(val hr: Int) : MonitoringAction()
    data class TriggerSitDownWarning(val hr: Int) : MonitoringAction()
    object StartWatchingCloser : MonitoringAction()
    object StopWatchingCloser : MonitoringAction()
}

object HeartRateEvaluator {
    
    fun evaluate(
        latestHr: Int,
        activityState: UserActivityState,
        settings: Settings,
        isWatchingCloser: Boolean
    ): MonitoringAction {
        val threshold = settings.effectiveThreshold
        val isStationary = activityState == UserActivityState.USER_ACTIVITY_PASSIVE

        // Logic for stopping HMS
        if (isWatchingCloser) {
            // HMS self-terminates if HR is back to normal (with 10 BPM buffer)
            // We could also add logic here to stop if user starts exercising.
            if (latestHr <= threshold - 10) {
                return MonitoringAction.StopWatchingCloser
            }
            return MonitoringAction.None
        }

        // Logic for triggering alerts/HMS from PMS
        
        // 1. High HR while stationary -> Start HMS and Alert
        if (isStationary && latestHr > threshold) {
            return MonitoringAction.StartWatchingCloser
        }

        // 2. Sit-down warning: if sick, not stationary, and HR is elevated.
        if (settings.isSickMode && !isStationary && latestHr > threshold) {
            return MonitoringAction.TriggerSitDownWarning(latestHr)
        }

        return MonitoringAction.None
    }
}
