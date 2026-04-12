package com.heart.sense.ui.ble

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heart.sense.service.BleSensorService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorSetupScreen(
    viewModel: BleSensorViewModel,
    onBack: () -> Unit
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val foundDevices by viewModel.foundDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val currentHr by viewModel.currentHr.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("External BLE Sensors") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Connect clinical-grade sensors (Polar H10, etc.) for high-fidelity research data.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Connection Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when(connectionState) {
                        BleSensorService.BleConnectionState.CONNECTED -> Color.Green.copy(alpha = 0.1f)
                        BleSensorService.BleConnectionState.CONNECTING -> Color.Yellow.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Status: $connectionState", fontWeight = FontWeight.Bold)
                    if (connectionState == BleSensorService.BleConnectionState.CONNECTED) {
                        Text(
                            text = "$currentHr BPM",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            onClick = { viewModel.disconnectDevice() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Disconnect")
                        }
                    } else if (connectionState == BleSensorService.BleConnectionState.DISCONNECTED) {
                        Button(
                            onClick = { viewModel.startScan() },
                            enabled = !isScanning
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning...")
                            } else {
                                Text("Scan for Devices")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (connectionState == BleSensorService.BleConnectionState.DISCONNECTED) {
                Text("Available Devices", style = MaterialTheme.typography.titleMedium)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(foundDevices) { device ->
                        DeviceItem(device) {
                            viewModel.connectDevice(device.address)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(device: BleDevice, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(device.name, fontWeight = FontWeight.Bold)
                Text(device.address, style = MaterialTheme.typography.labelSmall)
            }
            Text("Connect", color = MaterialTheme.colorScheme.primary)
        }
    }
}
