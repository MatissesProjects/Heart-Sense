package com.heart.sense.data

import com.heart.sense.data.db.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class FhirExporterTest {

    private lateinit var sessionDao: SessionDao
    private lateinit var alertDao: AlertDao
    private lateinit var measurementDao: OvernightMeasurementDao
    private lateinit var exportLogDao: FhirExportLogDao
    private lateinit var exporter: FhirExporter

    @Before
    fun setup() {
        sessionDao = mockk()
        alertDao = mockk()
        measurementDao = mockk()
        exportLogDao = mockk()
        
        coEvery { exportLogDao.insert(any()) } returns Unit
        
        exporter = FhirExporter(sessionDao, alertDao, measurementDao, exportLogDao)
    }

    @Test
    fun `exportVisitToFhirJson generates valid FHIR bundle`() = runTest {
        val visitId = "test-visit-123"
        
        val session = Session(
            visitId = visitId,
            startTime = LocalDateTime.now().minusHours(2),
            endTime = LocalDateTime.now(),
            clinicianNotes = "Test session notes"
        )
        
        val alert = Alert(
            id = 1,
            hr = 120,
            type = "High HR",
            visitId = visitId
        )

        coEvery { sessionDao.getSessionById(visitId) } returns session
        coEvery { alertDao.getAlertsByVisitId(visitId) } returns listOf(alert)
        coEvery { measurementDao.getMeasurementsByVisitId(visitId) } returns emptyList()

        val json = exporter.exportVisitToFhirJson(visitId)
        
        assertTrue(json != null)
        assertTrue(json!!.contains("Bundle"))
        assertTrue(json.contains("Encounter"))
        assertTrue(json.contains("Observation"))
        assertTrue(json.contains(visitId))
        assertTrue(json.contains("120"))
        assertTrue(json.contains("Test session notes"))
    }
}
