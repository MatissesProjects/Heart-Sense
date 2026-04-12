package com.heart.sense.wear.data

import com.heart.sense.wear.data.db.Medication
import com.heart.sense.wear.data.db.MedicationDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MedicationRepositoryTest {

    private lateinit var dao: MedicationDao
    private lateinit var commsRepo: WearableCommunicationRepository
    private lateinit var repository: MedicationRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        commsRepo = mockk(relaxed = true)
        repository = MedicationRepository(dao, commsRepo)
    }

    @Test
    fun `logIntake should send message to phone with formatted data`() = runTest {
        val med = Medication(id = 1, name = "Test Med", dose = "10mg", frequency = "Daily", reminderTime = "08:00")
        
        repository.logIntake(med)

        coVerify { 
            commsRepo.logIntakeToPhone(match { 
                it.startsWith("1|Test Med|") && it.endsWith("|10mg|Watch")
            }) 
        }
    }

    @Test
    fun `updateMedications should clear old and insert new`() = runTest {
        val meds = listOf(
            Medication(id = 1, name = "M1", dose = "1", frequency = "D", reminderTime = "T")
        )
        coEvery { dao.clearAll() } returns Unit
        coEvery { dao.insertAll(any()) } returns Unit

        repository.updateMedications(meds)

        coVerify { dao.clearAll() }
        coVerify { dao.insertAll(meds) }
    }
}
