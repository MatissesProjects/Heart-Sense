package com.heart.sense.wear.data

import com.heart.sense.wear.data.db.Medication
import com.heart.sense.wear.data.db.MedicationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val wearableCommunicationRepository: WearableCommunicationRepository
) {
    val allMedications: Flow<List<Medication>> = medicationDao.getAllMedications()

    suspend fun updateMedications(medications: List<Medication>) {
        medicationDao.clearAll()
        medicationDao.insertAll(medications)
    }

    suspend fun logIntake(medication: Medication) {
        val timestamp = System.currentTimeMillis()
        // Format: medId|medName|timestamp|dose|source
        val data = "${medication.id}|${medication.name}|$timestamp|${medication.dose}|Watch"
        wearableCommunicationRepository.logIntakeToPhone(data)
    }
}
