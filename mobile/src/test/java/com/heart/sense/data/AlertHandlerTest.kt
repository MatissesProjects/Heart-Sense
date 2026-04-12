package com.heart.sense.data

import android.content.Context
import android.util.Log
import com.heart.sense.util.NotificationHelper
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertHandlerTest {

    private lateinit var context: Context
    private lateinit var alertsRepository: AlertsRepository
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var localSyncRepository: LocalSyncRepository
    private lateinit var interventionRepository: InterventionRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var ambientSensorRepository: AmbientSensorRepository
    private lateinit var medicationRepository: MedicationRepository
    private lateinit var bloodGlucoseRepository: BloodGlucoseRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var alertHandler: AlertHandler

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        mockkConstructor(NotificationHelper::class)
        every { anyConstructed<NotificationHelper>().showHighHrNotification(any()) } returns Unit
        every { anyConstructed<NotificationHelper>().showCriticalHrNotification(any()) } returns Unit
        every { anyConstructed<NotificationHelper>().showSitDownWarning(any()) } returns Unit
        every { anyConstructed<NotificationHelper>().showIllnessNotification(any(), any(), any()) } returns Unit
        every { anyConstructed<NotificationHelper>().showStressNotification(any(), any(), any(), any()) } returns Unit
        every { anyConstructed<NotificationHelper>().showBehavioralNotification(any(), any()) } returns Unit
        every { anyConstructed<NotificationHelper>().showPrecursorNotification(any(), any()) } returns Unit
        every { anyConstructed<NotificationHelper>().showIrregularRhythmNotification() } returns Unit

        mockkObject(com.heart.sense.service.CbtTriggerWorker)
        every { com.heart.sense.service.CbtTriggerWorker.schedule(any(), any(), any()) } returns Unit

        context = mockk(relaxed = true)
        val notificationManager = mockk<android.app.NotificationManager>(relaxed = true)
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager

        alertsRepository = mockk(relaxed = true)
        coEvery { alertsRepository.addAlert(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns 1

        settingsDataStore = mockk(relaxed = true)
        localSyncRepository = mockk(relaxed = true)
        interventionRepository = mockk(relaxed = true)
        sessionRepository = mockk(relaxed = true)
        ambientSensorRepository = mockk(relaxed = true)
        medicationRepository = mockk(relaxed = true)
        bloodGlucoseRepository = mockk(relaxed = true)
        locationRepository = mockk(relaxed = true)
        weatherRepository = mockk(relaxed = true)
        
        every { ambientSensorRepository.getAmbientTemp() } returns flowOf(22.5f)
        every { ambientSensorRepository.getAmbientLux() } returns flowOf(300f)
        every { ambientSensorRepository.getAmbientNoise() } returns flowOf(45)
        every { weatherRepository.getLatestEnvironmentalContext() } returns flowOf(null)
    }

    private fun initHandler() {
        alertHandler = AlertHandler(
            context,
            alertsRepository,
            settingsDataStore,
            localSyncRepository,
            interventionRepository,
            sessionRepository,
            ambientSensorRepository,
            medicationRepository,
            bloodGlucoseRepository,
            locationRepository,
            weatherRepository
        )
    }

    @After
    fun tearDown() {
        // No resetMain here as we handle it per test in finally block
    }

    @Test
    fun `handleHrAlert adds alert and sends data when not snoozed`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            every { settingsDataStore.settings } returns flowOf(Settings())
            coEvery { sessionRepository.getActiveVisitId() } returns null
            coEvery { medicationRepository.getIntakesForDay(any()) } returns emptyList()
            every { medicationRepository.activeMedications } returns flowOf(emptyList())
            coEvery { bloodGlucoseRepository.isGlucoseCrashing() } returns false
            initHandler()

            val hr = 120
            alertHandler.handleHrAlert(hr)
            advanceUntilIdle()

            coVerify { alertsRepository.addAlert(hr = 120, type = any(), visitId = any(), ambientTemp = any(), ambientLux = any(), ambientDb = any(), aqi = any(), humidity = any(), barometricPressure = any()) }
            verify { localSyncRepository.sendData(any()) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `handleHrAlert does nothing when snoozed`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            // Set snoozeUntil to a future time
            val futureTime = System.currentTimeMillis() + 100000
            every { settingsDataStore.settings } returns flowOf(Settings(snoozeUntil = futureTime))
            coEvery { sessionRepository.getActiveVisitId() } returns null
            initHandler()
            
            val hr = 120
            alertHandler.handleHrAlert(hr)
            advanceUntilIdle()

            coVerify(exactly = 0) { alertsRepository.addAlert(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
            verify(exactly = 0) { localSyncRepository.sendData(any()) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `handleCriticalHrAlert adds alert and triggers emergency when enabled`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            every { settingsDataStore.settings } returns flowOf(Settings(isEmergencyEnabled = true, emergencyCountdownSeconds = 1))
            coEvery { sessionRepository.getActiveVisitId() } returns null
            initHandler()
            
            val hr = 160
            alertHandler.handleCriticalHrAlert(hr)
            advanceUntilIdle()

            coVerify { alertsRepository.addAlert(hr = 160, type = any(), visitId = any(), ambientTemp = any(), ambientLux = any(), ambientDb = any(), aqi = any(), humidity = any(), barometricPressure = any()) }
            verify { localSyncRepository.sendData(any()) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `handleStressAlert starts intervention and recommended technique`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val trigger = "Noise"
            val recommendation = "Breathing"
            every { settingsDataStore.settings } returns flowOf(Settings())
            coEvery { sessionRepository.getActiveVisitId() } returns null
            coEvery { interventionRepository.getRecommendation(trigger) } returns recommendation
            initHandler()

            alertHandler.handleStressAlert("High", 20, 5f, trigger)
            advanceUntilIdle()

            coVerify { interventionRepository.getRecommendation(trigger) }
            coVerify { 
                interventionRepository.startIntervention(
                    type = recommendation,
                    trigger = trigger,
                    hr = any(),
                    hrv = any(),
                    visitId = any()
                )
            }
            coVerify { alertsRepository.addAlert(hr = any(), type = any(), visitId = any(), ambientTemp = any(), ambientLux = any(), ambientDb = any(), aqi = any(), humidity = any(), barometricPressure = any()) }
        } finally {
            Dispatchers.resetMain()
        }
    }
}
