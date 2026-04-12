package com.heart.sense.data

import com.heart.sense.data.db.OvernightMeasurement
import com.heart.sense.data.db.OvernightMeasurementDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class DailyAverageRepositoryTest {

    private lateinit var dao: OvernightMeasurementDao
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: DailyAverageRepository

    @Before
    fun setup() {
        dao = mockk()
        settingsDataStore = mockk()
        
        every { settingsDataStore.settings } returns flowOf(Settings(highHrThreshold = 100, cyclePhase = "FOLLICULAR"))
        
        repository = DailyAverageRepository(dao, settingsDataStore)
    }

    @Test
    fun `calculateAdaptiveBaseline computes weighted average with cycle phase offset`() = runTest {
        val now = Instant.now()
        
        // Mock 3 days of measurements
        val m1 = OvernightMeasurement(
            timestamp = now.minus(2, ChronoUnit.DAYS).toEpochMilli(),
            heartRate = 60,
            respiratoryRate = 14f,
            activityState = 1
        )
        val m2 = OvernightMeasurement(
            timestamp = now.minus(1, ChronoUnit.DAYS).toEpochMilli(),
            heartRate = 70,
            respiratoryRate = 15f,
            activityState = 1
        )
        val m3 = OvernightMeasurement(
            timestamp = now.toEpochMilli(),
            heartRate = 65,
            respiratoryRate = 14f,
            activityState = 1
        )

        coEvery { dao.getMeasurementsInRange(any(), any()) } returns listOf(m1, m2, m3)

        // Day 1: HR 60 (weight 1)
        // Day 2: HR 70 (weight 2)
        // Day 3: HR 65 (weight 3)
        // Weighted Average = (60*1 + 70*2 + 65*3) / 6 = (60 + 140 + 195) / 6 = 395 / 6 = 65.83 -> 65
        // Phase Offset (FOLLICULAR) = -1
        // Expected Baseline = 65 - 1 = 64

        val baseline = repository.calculateAdaptiveBaseline()
        assertEquals(64, baseline)
    }
}
