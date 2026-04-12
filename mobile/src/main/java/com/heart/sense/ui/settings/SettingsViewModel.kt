package com.heart.sense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heart.sense.data.Alert
import com.heart.sense.data.AlertsRepository
import com.heart.sense.data.DailyAverage
import com.heart.sense.data.DailyAverageRepository
import com.heart.sense.data.HealthConnectRepository
import com.heart.sense.data.LocalSyncRepository
import com.heart.sense.data.MedicationRepository
import com.heart.sense.data.BloodGlucoseRepository
import com.heart.sense.data.SessionRepository
import com.heart.sense.data.Settings
import com.heart.sense.data.SettingsDataStore
import com.heart.sense.data.SettingsRepository
import com.heart.sense.data.EnvironmentalCorrelationRepository
import com.heart.sense.data.EnvironmentalInsight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val settingsDataStore: SettingsDataStore,
    private val alertsRepository: AlertsRepository,
    private val dailyAverageRepository: DailyAverageRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val localSyncRepository: LocalSyncRepository,
    private val medicationRepository: MedicationRepository,
    private val bloodGlucoseRepository: BloodGlucoseRepository,
    private val environmentalCorrelationRepository: EnvironmentalCorrelationRepository,
    private val fhirExporter: com.heart.sense.data.FhirExporter,
    private val cbtJournalDao: com.heart.sense.data.db.CbtJournalDao,
    val sessionRepository: SessionRepository
) : ViewModel() {
    
    val cbtJournalEntries: StateFlow<List<com.heart.sense.data.db.CbtJournalEntry>> = cbtJournalDao.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _cbtReflectionAlertId = MutableStateFlow<Int?>(null)
    val cbtReflectionAlertId = _cbtReflectionAlertId.asStateFlow()

    fun showCbtReflection(alertId: Int) {
        _cbtReflectionAlertId.value = alertId
    }

    fun dismissCbtReflection() {
        _cbtReflectionAlertId.value = null
    }

    suspend fun getAlertById(id: Int): Alert? = alertsRepository.getAlertById(id)

    fun saveCbtEntry(entry: com.heart.sense.data.db.CbtJournalEntry) {
        viewModelScope.launch {
            cbtJournalDao.insert(entry)
            dismissCbtReflection()
        }
    }

    val environmentalInsights: StateFlow<List<EnvironmentalInsight>> = environmentalCorrelationRepository.getEnvironmentalInsights()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _medicationIntakes = MutableStateFlow<List<com.heart.sense.data.db.MedicationIntake>>(emptyList())
    val medicationIntakes: StateFlow<List<com.heart.sense.data.db.MedicationIntake>> = _medicationIntakes.asStateFlow()

    private val _bloodGlucose = MutableStateFlow<List<com.heart.sense.data.db.BloodGlucose>>(emptyList())
    val bloodGlucose: StateFlow<List<com.heart.sense.data.db.BloodGlucose>> = _bloodGlucose.asStateFlow()

    fun refreshMedicationIntakes() {
        viewModelScope.launch {
            _medicationIntakes.value = medicationRepository.getIntakesForDay(System.currentTimeMillis())
        }
    }

    fun refreshBloodGlucose() {
        viewModelScope.launch {
            _bloodGlucose.value = bloodGlucoseRepository.getGlucoseInRange(
                System.currentTimeMillis() - (24 * 60 * 60 * 1000L),
                System.currentTimeMillis()
            )
        }
    }

    fun syncCgmData() {
        viewModelScope.launch {
            bloodGlucoseRepository.syncFromHealthConnect()
            refreshBloodGlucose()
        }
    }
    
    val activeSession: StateFlow<com.heart.sense.data.Session?> = sessionRepository.activeSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun startVisit(notes: String? = null) {
        viewModelScope.launch {
            sessionRepository.startSession(notes)
        }
    }

    fun endVisit() {
        viewModelScope.launch {
            sessionRepository.endSession()
        }
    }

    val settings: StateFlow<Settings> = settingsDataStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Settings()
    )

    private val _healthConnectPermissionsGranted = MutableStateFlow(false)
    val healthConnectPermissionsGranted: StateFlow<Boolean> = _healthConnectPermissionsGranted.asStateFlow()

    val healthConnectPermissions = healthConnectRepository.permissions

    val connectedNearbyDevices = localSyncRepository.connectedDevices
    val incomingNearbyData = localSyncRepository.incomingData

    fun checkHealthConnectPermissions() {
        viewModelScope.launch {
            _healthConnectPermissionsGranted.value = healthConnectRepository.hasAllPermissions()
        }
    }

    fun startBroadcasting(deviceName: String) {
        localSyncRepository.startBroadcasting(deviceName)
    }

    fun stopBroadcasting() {
        localSyncRepository.stopBroadcasting()
    }

    fun startDiscovery() {
        localSyncRepository.startDiscovery()
    }

    fun stopDiscovery() {
        localSyncRepository.stopDiscovery()
    }

    fun syncAllToHealthConnect() {
        viewModelScope.launch {
            val averages = dailyAverageRepository.getDailyAverages(30)
            averages.forEach { 
                healthConnectRepository.writeDailyAverage(it)
            }
            syncMenstrualCycle()
        }
    }

    private suspend fun syncMenstrualCycle() {
        val now = java.time.Instant.now()
        val past = now.minus(java.time.Duration.ofDays(40))
        val records = healthConnectRepository.readMenstruationRecords(past, now)
        val latest = records.maxByOrNull { it.startTime }
        val phase = if (latest != null) {
            val daysSinceStart = java.time.Duration.between(latest.startTime, now).toDays()
            when {
                daysSinceStart in 0..5 -> "MENSTRUAL"
                daysSinceStart in 6..13 -> "FOLLICULAR"
                daysSinceStart in 14..16 -> "OVULATION"
                daysSinceStart in 17..28 -> "LUTEAL"
                else -> "UNKNOWN"
            }
        } else {
            "UNKNOWN"
        }
        val currentSettings = settingsDataStore.settings.first()
        if (currentSettings.cyclePhase != phase) {
            settingsDataStore.updateSettings(currentSettings.copy(cyclePhase = phase))
            repository.updateSettings(currentSettings.copy(cyclePhase = phase))
        }
    }

    val alerts: StateFlow<List<Alert>> = alertsRepository.alerts
    val liveHr: StateFlow<Int?> = alertsRepository.liveHr

    private val _dailyAverages = MutableStateFlow<List<DailyAverage>>(emptyList())
    val dailyAverages: StateFlow<List<DailyAverage>> = _dailyAverages.asStateFlow()

    val aiBaseline: StateFlow<Int> = settingsDataStore.settings.map { it.restingHr }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        refreshDailyAverages()
        refreshMedicationIntakes()
        refreshBloodGlucose()
    }

    fun recalculateBaseline() {
        viewModelScope.launch {
            repository.refreshAdaptiveBaseline()
        }
    }

    fun refreshDailyAverages() {
        viewModelScope.launch {
            _dailyAverages.value = dailyAverageRepository.getDailyAverages(7)
            checkStreakReset()
        }
    }

    private fun checkStreakReset() {
        viewModelScope.launch {
            val current = settings.value
            // If the last update was more than 24 hours ago, reset current streak
            if (System.currentTimeMillis() - current.lastUpdated > 24 * 60 * 60 * 1000L) {
                repository.updateSettings(current.copy(currentStreakMinutes = 0))
            }
        }
    }
    
    val isWatchConnected: StateFlow<Boolean> = alertsRepository.lastMessageTimestamp.map { timestamp ->
        System.currentTimeMillis() - timestamp < 60000 // Connected if seen in last 60 seconds
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    fun updateThreshold(threshold: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(highHrThreshold = threshold)
            repository.updateSettings(updated)
        }
    }
    
    fun toggleSickMode(isSick: Boolean) {
        viewModelScope.launch {
            repository.updateSickMode(isSick)
        }
    }

    fun toggleSnooze() {
        viewModelScope.launch {
            val current = settings.value
            if (current.isSnoozed) {
                repository.updateSettings(current.copy(snoozeUntil = 0L))
            } else {
                repository.setSnooze(30)
            }
        }
    }

    fun startCalibration() {
        viewModelScope.launch {
            settingsDataStore.startCalibration()
            // Sync to watch
            repository.updateSettings(settings.value.copy(
                calibrationStatus = "CALIBRATING",
                calibrationStartTime = System.currentTimeMillis()
            ))
        }
    }

    fun testAlert() {
        viewModelScope.launch {
            alertsRepository.addAlert((70..150).random(), "Test Alert", getActiveVisitIdSync())
        }
    }

    private fun getActiveVisitIdSync(): String? {
        // Simple helper for testing, in real use we'd use the flow or repo
        return null
    }

    fun tagAlert(alertId: Int, tag: String) {
        alertsRepository.tagAlert(alertId, tag)
    }

    fun tagLatestAlert(tag: String) {
        viewModelScope.launch {
            val latest = alerts.value.firstOrNull()
            if (latest != null) {
                alertsRepository.tagAlert(latest.id, tag)
            }
            // Also broadcast to nearby devices if connected
            localSyncRepository.sendData(com.heart.sense.data.NearbyPayload(0, "VOICE_TAG:$tag"))
        }
    }

    fun updateEmergencySettings(
        name: String,
        phone: String,
        countdown: Int,
        enabled: Boolean
    ) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                emergencyContactName = name,
                emergencyContactPhone = phone,
                emergencyCountdownSeconds = countdown,
                isEmergencyEnabled = enabled
            )
            repository.updateSettings(updated)
        }
    }

    fun updateBehavioralSettings(pacing: Boolean, agitation: Boolean) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                detectPacing = pacing,
                detectAgitation = agitation
            )
            repository.updateSettings(updated)
        }
    }

    fun generateReport(context: android.content.Context): android.net.Uri? {
        val generator = com.heart.sense.util.ReportGenerator(context)
        return generator.generateCsvReport(alerts.value, dailyAverages.value)
    }

    suspend fun exportVisitToFhir(visitId: String): String? {
        return fhirExporter.exportVisitToFhirJson(visitId)
    }
}
