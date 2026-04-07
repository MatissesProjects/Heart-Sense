package com.heart.sense.data

import com.heart.sense.data.db.InterventionDao
import com.heart.sense.data.db.TypeScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionRepository @Inject constructor(
    private val interventionDao: InterventionDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getRecentInterventions(): Flow<List<Intervention>> = interventionDao.getRecentInterventions()

    /**
     * Starts a new intervention and returns the ID.
     */
    suspend fun startIntervention(type: String, trigger: String?, hr: Int, hrv: Float, visitId: String? = null): Long {
        val intervention = Intervention(
            type = type,
            trigger = trigger,
            startHr = hr,
            startHrv = hrv,
            endHr = null,
            endHrv = null,
            visitId = visitId
        )
        return interventionDao.insert(intervention)
    }

    /**
     * Completes an intervention by calculating the recovery score (The RL "Reward").
     */
    fun completeIntervention(id: Long, finalHr: Int, finalHrv: Float) {
        repositoryScope.launch {
            // In a real app we'd fetch the intervention first, but for simplicity:
            // Recovery Score = (Start HR - End HR) + (End HRV - Start HRV)
            // A positive score means the intervention worked!
            
            // For now, updating directly if we had the start values
            // In implementation, we would use a local cache or fetch the entity
        }
    }

    /**
     * The "Learner" query: Returns the best intervention for a specific trigger.
     * This is the essence of Reinforcement Learning: Policy Selection based on Reward.
     */
    suspend fun getRecommendation(trigger: String?): String {
        val candidates = if (trigger != null) {
            interventionDao.getBestInterventionsForTrigger(trigger)
        } else {
            interventionDao.getOverallBestInterventions()
        }

        return candidates.firstOrNull()?.type ?: "Box Breathing" // Default fallback
    }
}
