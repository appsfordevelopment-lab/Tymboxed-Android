package dev.ambitionsoftware.tymeboxed.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository
import dev.ambitionsoftware.tymeboxed.domain.model.Session
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Central place to end a focus session and stop the foreground blocking service
 * (used from [dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeViewModel] and
 * [SessionTimerHandler]).
 */
@Singleton
class AppSessionController @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val sessionRepository: SessionRepository,
) {

    suspend fun endSessionCompletely() = withContext(Dispatchers.IO) {
        ActiveBlockingState.deactivate()
        sessionRepository.resetActive()
        withContext(Dispatchers.Main) {
            appContext.startService(SessionBlockerService.stopIntent(appContext))
        }
    }

    suspend fun updateSessionEntity(session: Session) = withContext(Dispatchers.IO) {
        sessionRepository.update(session)
    }
}
