package com.heart.sense.data

import com.heart.sense.data.db.SessionDao
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class SessionRepositoryTest {

    private lateinit var sessionDao: SessionDao
    private lateinit var repository: SessionRepository

    @Before
    fun setup() {
        sessionDao = mockk(relaxed = true)
        repository = SessionRepository(sessionDao)
    }

    @Test
    fun `startSession inserts new session and returns visitId`() = runTest {
        val visitId = repository.startSession("Notes")
        
        assertNotNull(visitId)
        coVerify { sessionDao.insert(match { 
            it.visitId == visitId && it.clinicianNotes == "Notes" && it.endTime == null
        }) }
    }

    @Test
    fun `endSession updates active session with endTime`() = runTest {
        val activeSession = Session("v1", LocalDateTime.now(), null, "Notes")
        every { sessionDao.getActiveSession() } returns flowOf(activeSession)
        
        repository.endSession()
        
        coVerify { sessionDao.update(match { 
            it.visitId == "v1" && it.endTime != null
        }) }
    }

    @Test
    fun `getActiveVisitId returns id from dao`() = runTest {
        val activeSession = Session("v123", LocalDateTime.now(), null, null)
        every { sessionDao.getActiveSession() } returns flowOf(activeSession)
        
        val result = repository.getActiveVisitId()
        
        assertEquals("v123", result)
    }
}
