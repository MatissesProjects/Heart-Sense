package com.heart.sense.data

import com.google.gson.GsonBuilder
import com.heart.sense.data.db.AlertDao
import com.heart.sense.data.db.FhirExportLog
import com.heart.sense.data.db.FhirExportLogDao
import com.heart.sense.data.db.OvernightMeasurementDao
import com.heart.sense.data.db.SessionDao
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FhirExporter @Inject constructor(
    private val sessionDao: SessionDao,
    private val alertDao: AlertDao,
    private val measurementDao: OvernightMeasurementDao,
    private val exportLogDao: FhirExportLogDao
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun exportVisitToFhirJson(visitId: String): String? {
        val session = sessionDao.getSessionById(visitId) ?: return null
        val alerts = alertDao.getAlertsByVisitId(visitId)
        val measurements = measurementDao.getMeasurementsByVisitId(visitId)

        val entries = mutableListOf<Map<String, Any>>()

        // 1. Encounter Resource (Session)
        val encounter = mutableMapOf<String, Any>(
            "resourceType" to "Encounter",
            "id" to session.visitId,
            "status" to if (session.endTime != null) "finished" else "in-progress",
            "class" to mapOf(
                "system" to "http://terminology.hl7.org/CodeSystem/v3-ActCode",
                "code" to "AMB",
                "display" to "ambulatory"
            ),
            "period" to mutableMapOf<String, Any>(
                "start" to session.startTime.format(isoFormatter)
            )
        )
        session.endTime?.let { 
            (encounter["period"] as MutableMap<String, Any>)["end"] = it.format(isoFormatter) 
        }
        session.clinicianNotes?.let { 
            encounter["reasonCode"] = listOf(mapOf("text" to it))
        }
        
        entries.add(mapOf("resource" to encounter))

        // 2. Observation Resources (Alerts)
        alerts.forEach { alert ->
            val observation = mapOf(
                "resourceType" to "Observation",
                "id" to "alert-${alert.id}",
                "status" to "final",
                "category" to listOf(mapOf(
                    "coding" to listOf(mapOf(
                        "system" to "http://terminology.hl7.org/CodeSystem/observation-category",
                        "code" to "vital-signs",
                        "display" to "Vital Signs"
                    ))
                )),
                "code" to mapOf(
                    "coding" to listOf(mapOf(
                        "system" to "http://loinc.org",
                        "code" to "8867-4",
                        "display" to "Heart rate"
                    )),
                    "text" to alert.type
                ),
                "effectiveDateTime" to alert.timestamp.format(isoFormatter),
                "valueQuantity" to mapOf(
                    "value" to alert.hr,
                    "unit" to "beats/minute",
                    "system" to "http://unitsofmeasure.org",
                    "code" to "/min"
                ),
                "note" to listOfNotNull(
                    alert.tag?.let { mapOf("text" to "Tag: $it") },
                    alert.ambientTemp?.let { mapOf("text" to "Ambient Temp: $it C") }
                ),
                "encounter" to mapOf("reference" to "Encounter/${session.visitId}")
            )
            entries.add(mapOf("resource" to observation))
        }

        // 3. Observation Resources (Measurements)
        measurements.forEach { m ->
            val observation = mapOf(
                "resourceType" to "Observation",
                "id" to "measure-${m.id}",
                "status" to "final",
                "code" to mapOf(
                    "coding" to listOf(mapOf(
                        "system" to "http://loinc.org",
                        "code" to "8867-4",
                        "display" to "Heart rate"
                    ))
                ),
                "effectiveDateTime" to java.time.Instant.ofEpochMilli(m.timestamp)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(isoFormatter),
                "valueQuantity" to mapOf(
                    "value" to m.heartRate,
                    "unit" to "beats/minute",
                    "system" to "http://unitsofmeasure.org",
                    "code" to "/min"
                ),
                "encounter" to mapOf("reference" to "Encounter/${session.visitId}")
            )
            entries.add(mapOf("resource" to observation))
        }

        val bundle = mapOf(
            "resourceType" to "Bundle",
            "type" to "collection",
            "timestamp" to java.time.LocalDateTime.now().format(isoFormatter),
            "entry" to entries
        )

        val json = gson.toJson(bundle)
        
        // Log the export
        exportLogDao.insert(FhirExportLog(
            visitId = visitId,
            status = "SUCCESS",
            resourceCount = entries.size
        ))

        return json
    }
}
