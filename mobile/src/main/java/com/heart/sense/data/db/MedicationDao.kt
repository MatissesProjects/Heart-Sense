package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE isActive = 1")
    fun getActiveMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE isActive = 1")
    suspend fun getActiveMedicationsSync(): List<Medication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Query("DELETE FROM medications WHERE id = :medId")
    suspend fun deleteMedication(medId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: MedicationIntake)

    @Query("SELECT * FROM medication_intakes WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getIntakesInRange(startTime: Long, endTime: Long): List<MedicationIntake>

    @Query("SELECT * FROM medication_intakes WHERE medId = :medId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastIntakeForMed(medId: Int): MedicationIntake?
}
