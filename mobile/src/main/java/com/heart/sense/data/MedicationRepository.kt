package com.heart.sense.data

import com.heart.sense.data.db.Medication
import com.heart.sense.data.db.MedicationDao
import com.heart.sense.data.db.MedicationIntake
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val wearableCommunicationRepository: WearableCommunicationRepository
) {
    val activeMedications: Flow<List<Medication>> = medicationDao.getActiveMedications()

    suspend fun addMedication(medication: Medication) {
        medicationDao.insertMedication(medication)
        syncMedicationsToWatch()
    }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
        syncMedicationsToWatch()
    }

    suspend fun deleteMedication(medId: Int) {
        medicationDao.deleteMedication(medId)
        syncMedicationsToWatch()
    }

    suspend fun logIntake(intake: MedicationIntake) {
        medicationDao.insertIntake(intake)
    }

    suspend fun getIntakesForDay(timestamp: Long): List<MedicationIntake> {
        // Simple day range (ignoring TZ complexities for now)
        val startTime = (timestamp / (24 * 60 * 60 * 1000L)) * (24 * 60 * 60 * 1000L)
        val endTime = startTime + (24 * 60 * 60 * 1000L) - 1
        return medicationDao.getIntakesInRange(startTime, endTime)
    }

    suspend fun syncMedicationsToWatch() {
        // Implement flow first for real-time reactive sync later, 
        // but for now we manually trigger on changes.
        val medications = medicationDao.getActiveMedicationsSync()
        wearableCommunicationRepository.syncMedications(medications)
    }
}
