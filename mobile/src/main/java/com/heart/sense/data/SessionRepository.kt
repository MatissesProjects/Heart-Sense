package com.heart.sense.data

import com.heart.sense.data.db.SessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    val activeSession: Flow<Session?> = sessionDao.getActiveSession()
    val allSessions: Flow<List<Session>> = sessionDao.getAllSessions()

    suspend fun startSession(notes: String? = null): String {
        val visitId = UUID.randomUUID().toString()
        val session = Session(
            visitId = visitId,
            startTime = LocalDateTime.now(),
            clinicianNotes = notes
        )
        sessionDao.insert(session)
        return visitId
    }

    suspend fun endSession() {
        val active = sessionDao.getActiveSession().firstOrNull()
        if (active != null) {
            sessionDao.update(active.copy(endTime = LocalDateTime.now()))
        }
    }

    suspend fun getActiveVisitId(): String? {
        return sessionDao.getActiveSession().firstOrNull()?.visitId
    }
}
