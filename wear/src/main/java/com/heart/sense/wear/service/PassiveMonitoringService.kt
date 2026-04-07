package com.heart.sense.wear.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.*
import com.heart.sense.wear.data.SettingsDataStore
import com.heart.sense.wear.data.WearableCommunicationRepository
import com.heart.sense.wear.data.CalibrationRepository
import com.heart.sense.wear.data.OvernightDataRepository
import com.heart.sense.wear.data.MotionSensorRepository
import com.heart.sense.wear.data.EnvironmentalSensorRepository
import com.heart.sense.wear.data.AdvancedSensorRepository
import com.heart.sense.wear.data.GamificationRepository
import com.heart.sense.wear.util.HeartRateEvaluator
import com.heart.sense.wear.util.MonitoringAction
import com.heart.sense.wear.util.RhythmEvaluator
import com.heart.sense.wear.util.RhythmState
import com.heart.sense.wear.util.StressEvaluator
import com.heart.sense.wear.util.StressRisk
import com.heart.sense.wear.util.StressContext
import com.heart.sense.wear.util.FidgetDetector
import com.heart.sense.wear.util.PacingDetector
import com.heart.sense.wear.ai.MultiModalDataAggregator
import com.heart.sense.wear.ai.MultiModalPoint
import com.heart.sense.wear.ai.StressPredictor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PassiveMonitoringService : PassiveListenerService() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var wearableCommunicationRepository: WearableCommunicationRepository

    @Inject
    lateinit var calibrationRepository: CalibrationRepository

    @Inject
    lateinit var overnightDataRepository: OvernightDataRepository

    @Inject
    lateinit var motionSensorRepository: MotionSensorRepository

    @Inject
    lateinit var environmentalSensorRepository: EnvironmentalSensorRepository

    @Inject
    lateinit var advancedSensorRepository: AdvancedSensorRepository

    @Inject
    lateinit var dataAggregator: MultiModalDataAggregator

    @Inject
    lateinit var stressPredictor: StressPredictor

    @Inject
    lateinit var gamificationRepository: GamificationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var lastActivityState: UserActivityState = UserActivityState.USER_ACTIVITY_UNKNOWN
    private var isPaused: Boolean = false
    private var currentMotionScore: Float = 0f
    private var lastHr: Int = 0

    // Environmental Context
    private var currentLux: Float = 0f
    private var currentDb: Int = 0
    private var isSuddenNoise: Boolean = false
    private var isSuddenLight: Boolean = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d("PassiveMonitoring", "Screen OFF - Pausing monitoring (proxy for off-body)")
                    isPaused = true
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d("PassiveMonitoring", "Screen ON - Resuming monitoring (proxy for on-body)")
                    isPaused = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(receiver, filter)
        createNotificationChannel()
        startMotionTracking()
        startEnvironmentalTracking()
    }

    private fun startEnvironmentalTracking() {
        scope.launch {
            environmentalSensorRepository.getLightData().collect { lux ->
                if (lux > currentLux + 200 && currentLux > 0) {
                    isSuddenLight = true
                    scope.launch { kotlinx.coroutines.delay(5000); isSuddenLight = false }
                }
                currentLux = lux
            }
        }
        scope.launch {
            environmentalSensorRepository.getNoiseData().collect { db ->
                if (db > currentDb + 15 && currentDb > 0) {
                    isSuddenNoise = true
                    scope.launch { kotlinx.coroutines.delay(5000); isSuddenNoise = false }
                }
                currentDb = db
            }
        }
    }

    private fun startMotionTracking() {
        scope.launch {
            motionSensorRepository.getMotionData().collect { data ->
                val evaluation = FidgetDetector.process(data)
                currentMotionScore = evaluation.score
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onUserActivityInfoReceived(userActivityInfo: UserActivityInfo) {
        val newState = userActivityInfo.userActivityState
        Log.d("PassiveMonitoring", "Activity State: $newState")
        
        calibrationRepository.updateActivityState(newState)

        // Detect sleep-to-awake transition
        if (lastActivityState == UserActivityState.USER_ACTIVITY_ASLEEP && 
            (newState == UserActivityState.USER_ACTIVITY_PASSIVE)) {
            Log.d("PassiveMonitoring", "User woke up. Triggering illness detection check.")
            triggerIllnessDetection()
        }

        lastActivityState = newState

        // Pacing Detection
        scope.launch {
            val settings = settingsDataStore.settings.first()
            if (settings.detectPacing) {
                val isPacing = PacingDetector.process(newState, lastHr, settings.restingHr)
                if (isPacing) {
                    Log.w("PassiveMonitoring", "Pacing Detected! State: $newState, HR: $lastHr")
                    wearableCommunicationRepository.sendBehavioralAlert("Pacing", "Repetitive back-and-forth movement detected")
                }
            }
        }
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        if (isPaused) {
            Log.d("PassiveMonitoring", "Monitoring is paused, ignoring data points.")
            return
        }

        scope.launch {
            val isHMSActive = settingsDataStore.isMonitoringActive.first()
            if (isHMSActive) {
                Log.d("PassiveMonitoring", "HMS is active, suspending passive processing.")
                return@launch
            }
        
            val hrDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
            
            if (hrDataPoints.isEmpty()) return@launch

            val latestDataPoint = hrDataPoints.last()
            val latestHr = latestDataPoint.value.toInt()
            lastHr = latestHr
            
            // Extract RR intervals from metadata if available
            val rrIntervals = try {
                latestDataPoint.metadata.getLongArray("androidx.health.services.client.data.DataPoint.HEART_RATE_RR_INTERVALS")?.toList()
            } catch (e: Exception) {
                null
            }
            
            Log.d("PassiveMonitoring", "New HR: $latestHr BPM, RR Intervals: ${rrIntervals?.size ?: 0}")

            val settings = settingsDataStore.settings.first()

            // Sudden Agitation Detection
            if (settings.detectAgitation) {
                val isSuddenAgitation = (lastActivityState == UserActivityState.USER_ACTIVITY_PASSIVE || 
                                        lastActivityState == UserActivityState.USER_ACTIVITY_ASLEEP) &&
                                        latestHr > settings.restingHr + 30
                
                if (isSuddenAgitation) {
                    Log.w("PassiveMonitoring", "Sudden Agitation Detected! HR: $latestHr")
                    wearableCommunicationRepository.sendBehavioralAlert("Agitation", "Sudden HR spike while transitioning from rest")
                }
            }

            // Rhythm evaluation (only if stationary)
            if (lastActivityState == UserActivityState.USER_ACTIVITY_PASSIVE || 
                lastActivityState == UserActivityState.USER_ACTIVITY_ASLEEP) {
                val rhythmState = RhythmEvaluator.evaluate(rrIntervals)
                if (rhythmState == RhythmState.IRREGULAR) {
                    Log.w("PassiveMonitoring", "Irregular rhythm detected!")
                    triggerIrregularRhythmAlert()
                }
            }

            // Store for overnight analysis
            overnightDataRepository.storeMeasurement(
                heartRate = latestHr, 
                respiratoryRate = null, // RR Placeholder
                activityState = lastActivityState.id,
                rrIntervals = rrIntervals,
                motionIntensity = currentMotionScore
            )

            // Update Calm Streak / Gamification
            gamificationRepository.updateCalmState(latestHr)

            // Stress Evaluation (Autism Clinic Feature)
            val currentRmssd = StressEvaluator.calculateRmssd(rrIntervals)
            
            // Add to AI Data Aggregator
            dataAggregator.addPoint(MultiModalPoint(
                hr = latestHr,
                hrv = currentRmssd,
                motionScore = currentMotionScore,
                ambientNoise = currentDb
            ))

            // Run AI Prediction
            if (dataAggregator.isReady()) {
                val input = dataAggregator.getNormalizedInput(settings)
                val prediction = stressPredictor.predict(input)
                
                if (prediction.futureStressScore > 0.7f && prediction.confidence > 0.8f) {
                    Log.w("PassiveMonitoring", "AI PRECURSOR ALERT: Potential stress spike in 10 mins! Score: ${prediction.futureStressScore}")
                    wearableCommunicationRepository.sendPrecursorAlert(prediction.futureStressScore, prediction.confidence)
                }
            }

            val advancedData = advancedSensorRepository.sensorData.first()

            val stressResult = StressEvaluator.evaluate(
                currentHr = latestHr,
                currentRmssd = currentRmssd,
                settings = settings,
                activityState = lastActivityState,
                envContext = StressContext(
                    currentLux = currentLux,
                    currentDb = currentDb,
                    currentSkinTemp = advancedData.skinTemp ?: advancedData.manualTemp,
                    currentEda = advancedData.eda,
                    isSuddenNoise = isSuddenNoise,
                    isSuddenLight = isSuddenLight
                )
            )

            if (stressResult.risk == StressRisk.MODERATE || stressResult.risk == StressRisk.HIGH) {
                Log.w("PassiveMonitoring", "Stress Alert: ${stressResult.risk}. HR Delta: ${stressResult.hrDelta}, Trigger: ${stressResult.trigger}")
                wearableCommunicationRepository.sendStressAlert(
                    risk = stressResult.risk.name,
                    hrDelta = stressResult.hrDelta,
                    hrvDelta = stressResult.hrvDelta,
                    trigger = stressResult.trigger
                )

                if (stressResult.risk == StressRisk.HIGH) {
                    triggerStressIntervention()
                }
            }

            // Process for calibration
            calibrationRepository.processDataPoints(dataPoints)

            val action = HeartRateEvaluator.evaluate(
                latestHr = latestHr,
                activityState = lastActivityState,
                settings = settings,
                isWatchingCloser = false,
                respiratoryRate = null
            )

            when (action) {
                is MonitoringAction.StartWatchingCloser -> {
                    triggerHighHrAlert(latestHr)
                }
                is MonitoringAction.TriggerCriticalAlert -> {
                    wearableCommunicationRepository.sendCriticalHrAlert(action.hr)
                }
                is MonitoringAction.TriggerSitDownWarning -> {
                    triggerSitDownWarning(action.hr)
                }
                else -> {}
            }
        }
    }

    private fun triggerIllnessDetection() {
        scope.launch {
            val averages = overnightDataRepository.getOvernightAverages()
            val settings = settingsDataStore.settings.first()
            
            val result = com.heart.sense.wear.util.IllnessEvaluator.evaluate(averages, settings)
            if (result.risk != com.heart.sense.wear.util.IllnessRisk.NONE) {
                Log.d("PassiveMonitoring", "Illness Risk Detected: ${result.risk}. Sending alert.")
                wearableCommunicationRepository.sendIllnessAlert(
                    risk = result.risk.name,
                    hrElevation = result.hrElevation,
                    rrElevation = result.rrElevation
                )
            }
            
            // Clean up old data after calculation
            overnightDataRepository.deleteOldData()
        }
    }

    private fun triggerHighHrAlert(hr: Int) {
        val intent = Intent(this, HealthMonitoringService::class.java)
        startForegroundService(intent)
        scope.launch {
            wearableCommunicationRepository.sendHrAlert(hr)
        }
    }

    private fun triggerSitDownWarning(hr: Int) {
        scope.launch {
            wearableCommunicationRepository.sendSitDownWarning(hr)
        }
    }

    private fun triggerStressIntervention() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(300, createStressNotification())
    }

    private fun triggerIrregularRhythmAlert() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(200, createIrregularRhythmNotification())
        
        scope.launch {
            wearableCommunicationRepository.sendIrregularRhythmAlert()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "rhythm_alerts",
            "Heart Rhythm Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createIrregularRhythmNotification(): Notification {
        val ecgIntent = packageManager.getLaunchIntentForPackage("com.fitbit.ecg")
        val pendingIntent = if (ecgIntent != null) {
            PendingIntent.getActivity(this, 0, ecgIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            null
        }

        val builder = NotificationCompat.Builder(this, "rhythm_alerts")
            .setContentTitle("Irregular Rhythm")
            .setContentText("A possible irregular heart rhythm was detected. Please take an ECG.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (pendingIntent != null) {
            builder.addAction(android.R.drawable.ic_media_play, "Launch ECG", pendingIntent)
        }

        return builder.build()
    }

    private fun createStressNotification(): Notification {
        val intent = Intent(this, com.heart.sense.wear.MainActivity::class.java).apply {
            putExtra("show_biofeedback", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "rhythm_alerts") // Reuse channel for simplicity or create new
            .setContentTitle("Stress Spike")
            .setContentText("High stress detected. Start a calming exercise?")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_play, "Start Exercise", pendingIntent)
            .build()
    }
}
