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
import com.heart.sense.data.Session
import com.heart.sense.ui.settings.SettingsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionExportScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessions by viewModel.sessionRepository.allSessions.collectAsState(initial = emptyList())
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical Session Export") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Visit History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Select a visit below to generate an anonymized, EHR-ready JSON export.", style = MaterialTheme.typography.bodySmall)
            }

            if (sessions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No recorded sessions found.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            items(sessions) { session ->
                SessionItem(session, formatter) {
                    // Export Logic: In a full implementation, we'd query all data for this VisitID
                    // For now, simulating the JSON packaging
                    val json = """
                        {
                          "visitId": "${session.visitId}",
                          "startTime": "${session.startTime}",
                          "endTime": "${session.endTime}",
                          "system": "Heart-Sense Wearable",
                          "anonymized": true,
                          "note": "${session.clinicianNotes ?: ""}"
                        }
                    """.trimIndent()
                    
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, "EHR Export: ${session.visitId}")
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Session"))
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: Session, formatter: DateTimeFormatter, onExport: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.startTime.format(formatter), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("ID: ${session.visitId.take(8)}...", style = MaterialTheme.typography.labelSmall)
                if (session.endTime != null) {
                    Text("Completed", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                } else {
                    Text("In Progress", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
            Button(onClick = onExport) {
                Text("Export")
            }
        }
    }
}
