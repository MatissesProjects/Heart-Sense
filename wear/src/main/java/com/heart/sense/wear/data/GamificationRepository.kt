package com.heart.sense.wear.data

import android.util.Log
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    private var lastCalmCheckTime = System.currentTimeMillis()

    /**
     * Processes current heart rate to update streaks and points.
     * Called periodically during monitoring.
     */
    suspend fun updateCalmState(currentHr: Int) {
        val now = System.currentTimeMillis()
        val settings = settingsDataStore.settings.first()
        
        if (!settings.isCalibrated) return

        // A state is "Calm" if HR is within 15 BPM of resting baseline
        val isCalm = currentHr <= settings.restingHr + 15
        
        val elapsedMinutes = ((now - lastCalmCheckTime) / 60000).toInt()
        if (elapsedMinutes < 1) return

        if (isCalm) {
            val newStreak = settings.currentStreakMinutes + elapsedMinutes
            val newPoints = settings.calmPoints + (elapsedMinutes * 2) // 2 points per calm minute
            val newBest = if (newStreak > settings.bestStreakMinutes) newStreak else settings.bestStreakMinutes
            
            settingsDataStore.updateSettings(settings.copy(
                currentStreakMinutes = newStreak,
                bestStreakMinutes = newBest,
                calmPoints = newPoints
            ))
            Log.d("Gamification", "Streak increased! New streak: $newStreak min")
        } else {
            if (settings.currentStreakMinutes > 0) {
                Log.d("Gamification", "Streak broken at ${settings.currentStreakMinutes} min")
                settingsDataStore.updateSettings(settings.copy(currentStreakMinutes = 0))
            }
        }
        
        lastCalmCheckTime = now
    }
}
