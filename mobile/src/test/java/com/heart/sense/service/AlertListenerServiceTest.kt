package com.heart.sense.service

import com.google.android.gms.wearable.MessageEvent
import com.heart.sense.data.AlertHandler
import com.heart.sense.util.Constants
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AlertListenerServiceTest {

    private lateinit var alertHandler: AlertHandler
    private lateinit var service: AlertListenerService

    @Before
    fun setup() {
        alertHandler = mockk(relaxed = true)
        service = AlertListenerService()
        service.alertHandler = alertHandler
    }

    @Test
    fun `onMessageReceived calls handleHrAlert for HR_ALERT path`() {
        val event = mockk<MessageEvent>()
        every { event.path } returns Constants.PATH_HR_ALERT
        every { event.data } returns "120".toByteArray()

        service.onMessageReceived(event)

        verify { alertHandler.handleHrAlert(120) }
    }

    @Test
    fun `onMessageReceived calls handleCriticalHrAlert for CRITICAL_HR path`() {
        val event = mockk<MessageEvent>()
        every { event.path } returns Constants.PATH_CRITICAL_HR
        every { event.data } returns "160".toByteArray()

        service.onMessageReceived(event)

        verify { alertHandler.handleCriticalHrAlert(160) }
    }

    @Test
    fun `onMessageReceived calls handleStressAlert for STRESS_ALERT path`() {
        val event = mockk<MessageEvent>()
        every { event.path } returns Constants.PATH_STRESS_ALERT
        every { event.data } returns "High|20|5.0|Noise".toByteArray()

        service.onMessageReceived(event)

        verify { alertHandler.handleStressAlert("High", 20, 5.0f, "Noise") }
    }

    @Test
    fun `onMessageReceived calls handleIllnessAlert for ILLNESS_ALERT path`() {
        val event = mockk<MessageEvent>()
        every { event.path } returns Constants.PATH_ILLNESS_ALERT
        every { event.data } returns "Moderate|10|2.5".toByteArray()

        service.onMessageReceived(event)

        verify { alertHandler.handleIllnessAlert("Moderate", 10, 2.5f) }
    }

    @Test
    fun `onMessageReceived calls handleBehavioralAlert for BEHAVIORAL_ALERT path`() {
        val event = mockk<MessageEvent>()
        every { event.path } returns Constants.PATH_BEHAVIORAL_ALERT
        every { event.data } returns "Pacing|Sudden pacing detected".toByteArray()

        service.onMessageReceived(event)

        verify { alertHandler.handleBehavioralAlert("Pacing", "Sudden pacing detected") }
    }
}
