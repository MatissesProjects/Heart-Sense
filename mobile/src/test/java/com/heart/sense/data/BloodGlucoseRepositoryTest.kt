package com.heart.sense.data

import com.heart.sense.data.db.BloodGlucose
import com.heart.sense.data.db.BloodGlucoseDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BloodGlucoseRepositoryTest {

    private lateinit var dao: BloodGlucoseDao
    private lateinit var healthConnectRepo: HealthConnectRepository
    private lateinit var repository: BloodGlucoseRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        healthConnectRepo = mockk(relaxed = true)
        repository = BloodGlucoseRepository(dao, healthConnectRepo)
    }

    @Test
    fun `isGlucoseCrashing should return true when drop is significant`() = runTest {
        val now = System.currentTimeMillis()
        val records = listOf(
            BloodGlucose(timestamp = now, value = 5.0),
            BloodGlucose(timestamp = now - 15 * 60 * 1000L, value = 6.5),
            BloodGlucose(timestamp = now - 30 * 60 * 1000L, value = 7.5)
        )
        
        coEvery { dao.getRecordsInRange(any(), any()) } returns records

        val crashing = repository.isGlucoseCrashing()
        
        // 7.5 - 5.0 = 2.5, which is > 2.0
        assertTrue("Should detect glucose crash", crashing)
    }

    @Test
    fun `isGlucoseCrashing should return false when drop is small`() = runTest {
        val now = System.currentTimeMillis()
        val records = listOf(
            BloodGlucose(timestamp = now, value = 6.0),
            BloodGlucose(timestamp = now - 30 * 60 * 1000L, value = 6.5)
        )
        
        coEvery { dao.getRecordsInRange(any(), any()) } returns records

        val crashing = repository.isGlucoseCrashing()
        
        assertFalse("Should not detect small drop as crash", crashing)
    }
}
