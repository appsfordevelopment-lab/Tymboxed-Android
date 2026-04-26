package dev.ambitionsoftware.tymeboxed.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository
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
    private val profileRepository: ProfileRepository,
    private val sessionReminderScheduler: SessionReminderScheduler,
) {

    /**
     * Ends the focus session, stops the blocking service, and (per iOS
     * [StrategyManager] `case .ended`) schedules a local notification if
     * [dev.ambitionsoftware.tymeboxed.domain.model.Profile.reminderTimeSeconds] is set.
     */
    suspend fun endSessionCompletely() = withContext(Dispatchers.IO) {
        val active = sessionRepository.findActive()
        val profileForReminder = active?.let { profileRepository.findById(it.profileId) }
        ActiveBlockingState.deactivate()
        sessionRepository.resetActive()
        withContext(Dispatchers.Main) {
            appContext.startService(SessionBlockerService.stopIntent(appContext))
        }
        if (profileForReminder != null) {
            sessionReminderScheduler.scheduleAfterSessionEnd(profileForReminder)
        }
    }

    suspend fun updateSessionEntity(session: Session) = withContext(Dispatchers.IO) {
        sessionRepository.update(session)
    }
}
