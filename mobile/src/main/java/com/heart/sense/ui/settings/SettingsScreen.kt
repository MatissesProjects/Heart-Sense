package com.heart.sense.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import com.heart.sense.ui.dashboard.HealthDashboard
import com.heart.sense.data.Settings
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val liveHr by viewModel.liveHr.collectAsState()
    val isConnected by viewModel.isWatchConnected.collectAsState()
    val dailyAverages by viewModel.dailyAverages.collectAsState()
    val aiBaseline by viewModel.aiBaseline.collectAsState()
    val healthPermissionsGranted by viewModel.healthConnectPermissionsGranted.collectAsState()

    var showCaregiverDashboard by remember { mutableStateOf(false) }
    var showReportScreen by remember { mutableStateOf(false) }

    if (showCaregiverDashboard) {
        com.heart.sense.ui.caregiver.LocalDashboard(
            viewModel = viewModel,
            onBack = { showCaregiverDashboard = false }
        )
        return
    }

    if (showReportScreen) {
        com.heart.sense.ui.reports.ReportScreen(
            viewModel = viewModel,
            onBack = { showReportScreen = false }
        )
        return
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.checkHealthConnectPermissions()
    }

    LaunchedEffect(Unit) {
        viewModel.checkHealthConnectPermissions()
    }

    val calibrationDurationHours = if (settings.isCalibrating) {
        (System.currentTimeMillis() - settings.calibrationStartTime) / (1000 * 60 * 60)
    } else {
        0L
    }
    val estimatedCompletionHours = 48L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Heart-Sense", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusText = when {
                        isConnected && liveHr != null -> "Watch Connected: $liveHr BPM"
                        isConnected -> "Watch Connected"
                        else -> "Watch Disconnected"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (settings.isSnoozed) {
                        Text(
                            text = "ALERTS SNOOZED: ${settings.snoozeRemainingMinutes}m left",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            com.heart.sense.ui.dashboard.CalmGardenCard(settings = settings)
        }

        if (settings.isCalibrating || settings.isCalibrated) {
            item {
                CalibrationProgressCard(
                    settings = settings,
                    durationHours = calibrationDurationHours,
                    totalHours = estimatedCompletionHours
                )
            }
        }
        
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Threshold: ${settings.highHrThreshold} bpm")
                    Slider(
                        value = settings.highHrThreshold.toFloat(),
                        onValueChange = { viewModel.updateThreshold(it.toInt()) },
                        valueRange = 60f..180f,
                        steps = 24,
                        enabled = !settings.isCalibrating // Threshold is automatic during/after calibration
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Sick Mode")
                        Switch(
                            checked = settings.isSickMode,
                            onCheckedChange = { viewModel.toggleSickMode(it) }
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleSnooze() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settings.isSnoozed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (settings.isSnoozed) "Clear Snooze" else "Snooze Alerts (30m)")
                    }

                    if (!settings.isCalibrated && !settings.isCalibrating) {
                        Button(
                            onClick = { viewModel.startCalibration() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Baseline Calibration (48h)")
                        }
                    }
                }
            }
        }

        item {
            EmergencyResponseCard(
                settings = settings,
                onUpdate = { name, phone, countdown, enabled ->
                    viewModel.updateEmergencySettings(name, phone, countdown, enabled)
                }
            )
        }

        item {
            BehavioralDetectionCard(
                settings = settings,
                onUpdate = { pacing, agitation ->
                    viewModel.updateBehavioralSettings(pacing, agitation)
                }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showCaregiverDashboard = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Local Caregiver Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Monitor other devices on your local network.", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showReportScreen = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clinical Reporting", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Generate and share summary reports for clinicians.", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }

        item {
            HealthConnectCard(
                permissionsGranted = healthPermissionsGranted,
                onRequestPermissions = { 
                    permissionLauncher.launch(viewModel.healthConnectPermissions)
                },
                onSync = { viewModel.syncAllToHealthConnect() }
            )
        }

        item {
            Button(onClick = { viewModel.testAlert() }) {
                Text("Send Test Alert")
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Adaptive Health Baseline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Your current AI Baseline Resting HR is $aiBaseline BPM. This is used to automatically set your alert thresholds.", style = MaterialTheme.typography.bodySmall)
                    
                    Button(
                        onClick = { viewModel.recalculateBaseline() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = dailyAverages.size >= 1
                    ) {
                        Text("Recalculate Baseline (7-Day)")
                    }
                }
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            HealthDashboard(dailyAverages = dailyAverages, settings = settings)
        }

        item {
            HorizontalDivider()
        }
        
        item {
            Text("Recent Alerts", style = MaterialTheme.typography.titleMedium)
        }
        
        items(alerts) { alert ->
            AlertItem(
                alert = alert,
                onTagSelected = { tag -> viewModel.tagAlert(alert.id, tag) }
            )
        }
    }
}

@Composable
fun BehavioralDetectionCard(
    settings: Settings,
    onUpdate: (Boolean, Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Behavioral Detection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Detect complex behaviors like pacing or sudden agitation by combining motion data and heart rate.",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Detect Pacing", style = MaterialTheme.typography.bodyLarge)
                    Text("Identify repetitive back-and-forth movement.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = settings.detectPacing,
                    onCheckedChange = { onUpdate(it, settings.detectAgitation) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Detect Agitation", style = MaterialTheme.typography.bodyLarge)
                    Text("Alert on sudden transitions with HR spikes.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = settings.detectAgitation,
                    onCheckedChange = { onUpdate(settings.detectPacing, it) }
                )
            }
        }
    }
}

@Composable
fun HealthConnectCard(
    permissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onSync: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Health Connect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Sync your heart rate and health data with the Android ecosystem.",
                style = MaterialTheme.typography.bodySmall
            )
            
            if (!permissionsGranted) {
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permissions")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✅ Connected", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onSync) {
                        Text("Sync Now")
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyResponseCard(
    settings: Settings,
    onUpdate: (String, String, Int, Boolean) -> Unit
) {
    var name by remember(settings.emergencyContactName) { mutableStateOf(settings.emergencyContactName) }
    var phone by remember(settings.emergencyContactPhone) { mutableStateOf(settings.emergencyContactPhone) }
    var countdown by remember(settings.emergencyCountdownSeconds) { mutableFloatStateOf(settings.emergencyCountdownSeconds.toFloat()) }
    var enabled by remember(settings.isEmergencyEnabled) { mutableStateOf(settings.isEmergencyEnabled) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Emergency Response", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Switch(
                    checked = enabled,
                    onCheckedChange = { 
                        enabled = it
                        onUpdate(name, phone, countdown.toInt(), it)
                    }
                )
            }

            Text(
                "Automated escalation to your emergency contact during critical alerts if you don't acknowledge them.",
                style = MaterialTheme.typography.bodySmall
            )

            if (enabled) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        onUpdate(it, phone, countdown.toInt(), enabled)
                    },
                    label = { Text("Contact Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it
                        onUpdate(name, it, countdown.toInt(), enabled)
                    },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Text("Escalation Countdown: ${countdown.toInt()}s", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = countdown,
                        onValueChange = { 
                            countdown = it
                            onUpdate(name, phone, it.toInt(), enabled)
                        },
                        valueRange = 10f..120f,
                        steps = 11
                    )
                }
            }
        }
    }
}

@Composable
fun CalibrationProgressCard(settings: Settings, durationHours: Long, totalHours: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Calibration Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            if (settings.isCalibrating) {
                val progress = (durationHours.toFloat() / totalHours.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Text("${durationHours}h collected of ${totalHours}h", style = MaterialTheme.typography.bodySmall)
                Text("Est. ${totalHours - durationHours}h remaining", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("✅ Calibration Complete", color = MaterialTheme.colorScheme.primary)
                Text("Resting HR: ${settings.restingHr} BPM", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun AlertItem(
    alert: com.heart.sense.data.Alert,
    onTagSelected: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val tags = listOf("Sensory", "Transition", "Anxiety", "Activity", "Other")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(alert.type, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(alert.timestamp.format(formatter), style = MaterialTheme.typography.bodySmall)
                }
                Text("${alert.hr} BPM", style = MaterialTheme.typography.headlineSmall)
            }
            
            if (alert.tag != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Tag: ${alert.tag}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        AssistChip(
                            onClick = { onTagSelected(tag) },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}
