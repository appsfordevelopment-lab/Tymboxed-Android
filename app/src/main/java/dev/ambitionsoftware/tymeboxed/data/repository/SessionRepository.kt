package dev.ambitionsoftware.tymeboxed.data.repository

import dev.ambitionsoftware.tymeboxed.data.db.dao.SessionDao
import dev.ambitionsoftware.tymeboxed.domain.model.Session
import dev.ambitionsoftware.tymeboxed.domain.model.toDomain
import dev.ambitionsoftware.tymeboxed.domain.model.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
) {
    val activeSession: Flow<Session?> =
        sessionDao.observeActive().map { it?.toDomain() }

    suspend fun findActive(): Session? = sessionDao.findActive()?.toDomain()

    suspend fun insert(session: Session) {
        sessionDao.insert(session.toEntity())
    }

    suspend fun update(session: Session) {
        sessionDao.update(session.toEntity())
    }

    /**
     * Closes any lingering active session (end-time set to now). Used by
     * the Settings > Troubleshooting > Reset Blocking State action. Phase 3
     * will also tear down the foreground service and AccessibilityService
     * state on the same call.
     */
    suspend fun resetActive() {
        sessionDao.endAllActive(System.currentTimeMillis())
    }

    suspend fun countCompletedForProfile(profileId: String): Int =
        sessionDao.countCompletedForProfile(profileId)
}
