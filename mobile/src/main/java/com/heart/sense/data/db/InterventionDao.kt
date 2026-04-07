package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heart.sense.data.Intervention
import kotlinx.coroutines.flow.Flow

@Dao
interface InterventionDao {
    @Insert
    suspend fun insert(intervention: Intervention): Long

    @Update
    suspend fun update(intervention: Intervention)

    @Query("SELECT * FROM interventions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentInterventions(): Flow<List<Intervention>>

    @Query("SELECT type, AVG(recoveryScore) as avgScore FROM interventions WHERE trigger = :trigger GROUP BY type ORDER BY avgScore DESC")
    suspend fun getBestInterventionsForTrigger(trigger: String): List<TypeScore>

    @Query("SELECT type, AVG(recoveryScore) as avgScore FROM interventions GROUP BY type ORDER BY avgScore DESC")
    suspend fun getOverallBestInterventions(): List<TypeScore>
}

data class TypeScore(
    val type: String,
    val avgScore: Float
)
