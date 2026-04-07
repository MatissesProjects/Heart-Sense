package com.heart.sense.ui.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heart.sense.data.NearbyDevice
import com.heart.sense.data.NearbyPayload
import com.heart.sense.ui.settings.SettingsViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDashboard(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val devices by viewModel.connectedNearbyDevices.collectAsState()
    val incomingData by viewModel.incomingNearbyData.collectAsState()
    
    var isDiscovering by remember { mutableStateOf(false) }
    var isBroadcasting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Caregiver Sync") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Role Selection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Choose whether this device is being monitored (Wearer) or is monitoring someone else (Caregiver).", style = MaterialTheme.typography.bodySmall)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Caregiver Mode (Discover)", style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    if (isDiscovering) viewModel.stopDiscovery() else viewModel.startDiscovery()
                                    isDiscovering = !isDiscovering
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDiscovering) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(if (isDiscovering) "Stop Discovery" else "Start Discovery")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Wearer Mode (Broadcast)", style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    if (isBroadcasting) viewModel.stopBroadcasting() else viewModel.startBroadcasting(android.os.Build.MODEL)
                                    isBroadcasting = !isBroadcasting
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBroadcasting) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(if (isBroadcasting) "Start Broadcasting" else "Broadcast My Data")
                            }
                        }
                    }
                }
            }

            if (devices.isNotEmpty()) {
                item {
                    Text("Connected Devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(devices) { device ->
                    DeviceItem(device)
                }
            }

            if (incomingData != null) {
                item {
                    Text("Live Remote Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    RemoteDataCard(incomingData!!)
                }
            }
        }
    }
}

@Composable
fun DeviceItem(device: NearbyDevice) {
    ListItem(
        headlineContent = { Text(device.name) },
        supportingContent = { Text("Status: ${device.status}") },
        trailingContent = {
            if (device.status == "Connected") {
                Text("✅", style = MaterialTheme.typography.headlineSmall)
            }
        }
    )
}

@Composable
fun RemoteDataCard(payload: NearbyPayload) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
    val time = formatter.format(Instant.ofEpochMilli(payload.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (payload.alert != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Last Update: $time", style = MaterialTheme.typography.labelSmall)
            Text("${payload.hr} BPM", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            if (payload.alert != null) {
                Text("ALERT: ${payload.alert}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
