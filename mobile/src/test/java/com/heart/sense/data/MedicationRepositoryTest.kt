package com.heart.sense.data

import com.heart.sense.data.db.Medication
import com.heart.sense.data.db.MedicationDao
import com.heart.sense.data.db.MedicationIntake
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
    fun `addMedication should insert into dao and sync to watch`() = runTest {
        val med = Medication(id = 1, name = "Test Med", dose = "10mg", frequency = "Daily", reminderTime = "08:00")
        coEvery { dao.insertMedication(any()) } returns 1L
        coEvery { dao.getActiveMedicationsSync() } returns listOf(med)

        repository.addMedication(med)

        coVerify { dao.insertMedication(med) }
        coVerify { commsRepo.syncMedications(listOf(med)) }
    }

    @Test
    fun `getIntakesForDay should calculate correct range`() = runTest {
        // Sample timestamp: 2026-04-12 10:00:00 UTC (1776000000000 approx)
        val timestamp = 1776000000000L
        // Expected start: 2026-04-12 00:00:00 (1775952000000)
        // Expected end: 2026-04-12 23:59:59 (1776038399999)
        
        coEvery { dao.getIntakesInRange(any(), any()) } returns emptyList()

        repository.getIntakesForDay(timestamp)

        coVerify { dao.getIntakesInRange(match { it % (24*60*60*1000L) == 0L }, any()) }
    }
}
