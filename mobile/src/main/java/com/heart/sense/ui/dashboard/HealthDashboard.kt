package com.heart.sense.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heart.sense.data.DailyAverage
import com.heart.sense.data.Settings
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.time.format.DateTimeFormatter

@Composable
fun HealthDashboard(
    dailyAverages: List<DailyAverage>,
    medicationIntakes: List<com.heart.sense.data.db.MedicationIntake>,
    settings: Settings,
    modifier: Modifier = Modifier
) {
    if (dailyAverages.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No data available yet", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Heart Rate (BPM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        HrChart(dailyAverages, settings)
        
        if (medicationIntakes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Medication Logs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            medicationIntakes.forEach { intake ->
                Text(
                    text = "💊 ${intake.medName} (${intake.dose}) at ${java.time.Instant.ofEpochMilli(intake.timestamp).atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("HRV (RMSSD ms)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        HrvChart(dailyAverages)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Daily Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        SummaryHeader()
        dailyAverages.reversed().forEach { average ->
            DailyAverageRow(average)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
        }
    }
}

@Composable
fun HrChart(dailyAverages: List<DailyAverage>, settings: Settings) {
    val modelProducer = remember { ChartEntryModelProducer() }
    
    LaunchedEffect(dailyAverages) {
        val entries = dailyAverages.mapIndexed { index, average ->
            FloatEntry(index.toFloat(), average.avgHr.toFloat())
        }
        modelProducer.setEntries(entries)
    }

    val thresholdLine = ThresholdLine(
        thresholdValue = settings.highHrThreshold.toFloat(),
        lineComponent = shapeComponent(color = Color.Red.copy(alpha = 0.5f)),
        labelComponent = textComponent(color = Color.Red),
        thresholdLabel = "Threshold"
    )

    Chart(
        chart = lineChart(
            decorations = listOf(thresholdLine)
        ),
        chartModelProducer = modelProducer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                dailyAverages.getOrNull(value.toInt())?.date?.format(DateTimeFormatter.ofPattern("MM/dd")) ?: ""
            }
        ),
        modifier = Modifier.height(200.dp)
    )
}

@Composable
fun HrvChart(dailyAverages: List<DailyAverage>) {
    val modelProducer = remember { ChartEntryModelProducer() }
    
    LaunchedEffect(dailyAverages) {
        val entries = dailyAverages.mapIndexed { index, average ->
            FloatEntry(index.toFloat(), average.hrvRmssd)
        }
        modelProducer.setEntries(entries)
    }

    Chart(
        chart = lineChart(),
        chartModelProducer = modelProducer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                dailyAverages.getOrNull(value.toInt())?.date?.format(DateTimeFormatter.ofPattern("MM/dd")) ?: ""
            }
        ),
        modifier = Modifier.height(200.dp)
    )
}

@Composable
fun SummaryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Date", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("HR", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
        Text("HRV", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
        Text("Status", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DailyAverageRow(average: DailyAverage) {
    val backgroundColor = if (average.isAlertTriggered) Color.Red.copy(alpha = 0.1f) else Color.Transparent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(average.date.format(DateTimeFormatter.ofPattern("MMM dd")), modifier = Modifier.weight(1f))
        Text("${average.avgHr}", modifier = Modifier.weight(0.8f))
        Text("${average.hrvRmssd.toInt()}", modifier = Modifier.weight(0.8f))
        
        val statusText = if (average.isAlertTriggered) {
            average.alertType ?: "Alert"
        } else {
            "Normal"
        }
        val statusColor = if (average.isAlertTriggered) Color.Red else Color.Green
        
        Text(
            statusText,
            modifier = Modifier.weight(1.2f),
            color = statusColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
