package com.heart.sense.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.heart.sense.data.Alert
import com.heart.sense.data.DailyAverage
import java.io.File
import java.time.format.DateTimeFormatter

class ReportGenerator(private val context: Context) {

    fun generateCsvReport(alerts: List<Alert>, averages: List<DailyAverage>): Uri? {
        val fileName = "heart_sense_report_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            file.bufferedWriter().use { out ->
                // Section 1: Daily Averages
                out.write("SECTION: DAILY AVERAGES\n")
                out.write("Date,Avg HR,Avg RR,HRV (RMSSD),Alert Triggered,Alert Type\n")
                averages.forEach { avg ->
                    out.write("${avg.date},${avg.avgHr},${avg.avgRr},${avg.hrvRmssd},${avg.isAlertTriggered},${avg.alertType ?: "None"}\n")
                }
                
                out.write("\n\n")
                
                // Section 2: Recent Alerts & Stress Events
                out.write("SECTION: RECENT ALERTS & EVENTS\n")
                out.write("Timestamp,Type,HR,Tag\n")
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                alerts.forEach { alert ->
                    out.write("${alert.timestamp.format(formatter)},${alert.type},${alert.hr},${alert.tag ?: ""}\n")
                }
            }
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
