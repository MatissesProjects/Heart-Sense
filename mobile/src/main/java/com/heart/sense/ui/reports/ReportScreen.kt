package com.heart.sense.ui.reports

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heart.sense.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val alerts by viewModel.alerts.collectAsState()
    val dailyAverages by viewModel.dailyAverages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical Report") },
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
                Text("Report Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("This summary aggregates physiological data and behavioral tags for clinical review.", style = MaterialTheme.typography.bodySmall)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Statistics (Last 7 Days)", style = MaterialTheme.typography.titleSmall)
                        Text("Total Alerts: ${alerts.size}")
                        Text("Average Resting HR: ${dailyAverages.map { it.avgHr }.filter { it > 0 }.average().toInt()} BPM")
                        Text("Tagged Events: ${alerts.count { it.tag != null }}")
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val uri = viewModel.generateReport(context)
                        if (uri != null) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "Heart-Sense Clinical Report")
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Share CSV Report")
                }
            }

            item {
                HorizontalDivider()
                Text("Data Preview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            items(alerts.take(10)) { alert ->
                ListItem(
                    headlineContent = { Text(alert.type) },
                    supportingContent = { Text("${alert.timestamp} - HR: ${alert.hr}") },
                    trailingContent = { alert.tag?.let { Text(it, color = MaterialTheme.colorScheme.primary) } }
                )
            }
        }
    }
}
