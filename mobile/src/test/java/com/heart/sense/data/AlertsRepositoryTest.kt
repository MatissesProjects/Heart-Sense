package com.heart.sense.data

import com.heart.sense.data.db.AlertDao
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsRepositoryTest {

    private lateinit var alertDao: AlertDao
    private lateinit var repository: AlertsRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        alertDao = mockk(relaxed = true)
        every { alertDao.getRecentAlerts() } returns flowOf(emptyList())
        repository = AlertsRepository(alertDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addAlert inserts alert into dao`() = runTest {
        repository.addAlert(120, "High HR", "visit123")
        
        coVerify { alertDao.insert(match { 
            it.hr == 120 && it.type == "High HR" && it.visitId == "visit123" 
        }) }
    }

    @Test
    fun `tagAlert updates tag in dao`() = runTest {
        repository.tagAlert(1, "Exercise")
        
        coVerify { alertDao.updateTag(1, "Exercise") }
    }

    @Test
    fun `updateLiveHr updates liveHr flow and timestamp`() {
        repository.updateLiveHr(85)
        
        assertEquals(85, repository.liveHr.value)
        assert(repository.lastMessageTimestamp.value > 0)
    }
}
