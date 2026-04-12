package com.heart.sense.data

import com.heart.sense.data.db.OvernightMeasurement
import com.heart.sense.data.db.OvernightMeasurementDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DailyAverageRepositoryTest {

    private lateinit var dao: OvernightMeasurementDao
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: DailyAverageRepository

    @Before
    fun setup() {
        dao = mockk()
        settingsDataStore = mockk()
        repository = DailyAverageRepository(dao, settingsDataStore)
    }

    @Test
    fun `getDailyAverages should calculate averages correctly for multiple days`() = runTest {
        // Mock data for 2 days
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterday = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val measurements = listOf(
            OvernightMeasurement(timestamp = today, heartRate = 70, respiratoryRate = 16f, activityState = 0),
            OvernightMeasurement(timestamp = today + 1000, heartRate = 80, respiratoryRate = 18f, activityState = 0),
            OvernightMeasurement(timestamp = yesterday, heartRate = 60, respiratoryRate = 14f, activityState = 0)
        )

        coEvery { dao.getMeasurementsInRange(any(), any()) } returns measurements
        every { settingsDataStore.settings } returns flowOf(Settings(highHrThreshold = 100))

        val results = repository.getDailyAverages(7)

        assertEquals(2, results.size)
        
        val todayAvg = results.find { it.date == LocalDate.now() }
        assertEquals(75, todayAvg?.avgHr) // (70+80)/2
        assertEquals(17f, todayAvg?.avgRr ?: 0f, 0.1f)
        
        val yesterdayAvg = results.find { it.date == LocalDate.now().minusDays(1) }
        assertEquals(60, yesterdayAvg?.avgHr)
    }

    @Test
    fun `calculateAdaptiveBaseline should give more weight to recent days`() = runTest {
        // Mock 3 days of averages
        val measurements = mutableListOf<OvernightMeasurement>()
        val today = LocalDate.now()
        
        // Day 1 (6 days ago): HR 60
        // Day 2 (1 day ago): HR 70
        // Day 3 (today): HR 80
        
        val d1 = today.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val d2 = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val d3 = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        measurements.add(OvernightMeasurement(timestamp = d1, heartRate = 60, respiratoryRate = 15f, activityState = 0))
        measurements.add(OvernightMeasurement(timestamp = d2, heartRate = 70, respiratoryRate = 15f, activityState = 0))
        measurements.add(OvernightMeasurement(timestamp = d3, heartRate = 80, respiratoryRate = 15f, activityState = 0))

        coEvery { dao.getMeasurementsInRange(any(), any()) } returns measurements
        every { settingsDataStore.settings } returns flowOf(Settings(highHrThreshold = 100))

        val baseline = repository.calculateAdaptiveBaseline()

        // Weighted Average Calculation:
        // Indices in sorted results: 0 (Day 1), 1 (Day 2), 2 (Day 3)
        // Weights: 1, 2, 3
        // (60*1 + 70*2 + 80*3) / (1+2+3) = (60 + 140 + 240) / 6 = 440 / 6 = 73.33 -> 73
        assertEquals(73, baseline)
    }

    @Test
    fun `getBaselineDeviation should calculate percentage correctly`() = runTest {
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        coEvery { dao.getMeasurementsInRange(any(), any()) } returns listOf(
            OvernightMeasurement(timestamp = today, heartRate = 75, respiratoryRate = 15f, activityState = 0)
        )
        // Resting HR in settings is 60. Current baseline is 75.
        // Deviation: (75 - 60) / 60 = 15 / 60 = 0.25
        every { settingsDataStore.settings } returns flowOf(Settings(restingHr = 60))

        val deviation = repository.getBaselineDeviation()
        assertEquals(0.25f, deviation, 0.01f)
    }
}
