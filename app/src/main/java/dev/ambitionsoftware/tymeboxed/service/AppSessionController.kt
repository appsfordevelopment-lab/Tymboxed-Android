package dev.ambitionsoftware.tymeboxed.service

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository
import dev.ambitionsoftware.tymeboxed.domain.model.BlockingStrategyId
import dev.ambitionsoftware.tymeboxed.domain.model.Session
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Central place to start / end a focus session and stop the foreground blocking service
 * (used from [dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeViewModel],
 * [SessionTimerHandler], and schedule alarms).
 */
@Singleton
class AppSessionController @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val sessionReminderScheduler: SessionReminderScheduler,
) {

    /**
     * Starts a focus session for [profileId]. Ends any other active session first.
     * When [selectedTimerMinutes] is set, updates focus-timer profile duration (iOS parity).
     */
    suspend fun startFocusSession(
        profileId: String,
        selectedTimerMinutes: Int? = null,
    ) = withContext(Dispatchers.IO) {
        sessionReminderScheduler.cancelAll()
        var profile = profileRepository.findById(profileId) ?: return@withContext

        if (selectedTimerMinutes != null &&
            profile.strategyId in listOf(BlockingStrategyId.FOCUS_TIMER, BlockingStrategyId.FOCUS_TIMER_BREAK)
        ) {
            val updated = profile.copy(
                strategyData = selectedTimerMinutes.coerceIn(1, 24 * 60).toString(),
                updatedAt = System.currentTimeMillis(),
            )
            profileRepository.save(updated)
            profile = updated
        }

        sessionRepository.resetActive()

        val now = System.currentTimeMillis()
        val session = Session(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            startTime = now,
        )
        sessionRepository.insert(session)

        BlockingStateRestorer.apply(
            profile = profile,
            session = session,
            blockedPackages = profile.blockedPackages.toSet(),
        )

        val serviceIntent = SessionBlockerService.startIntent(
            appContext,
            profile.name,
            session.startTime,
        )
        withContext(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(serviceIntent)
            } else {
                appContext.startService(serviceIntent)
            }
        }
    }

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
            @Suppress("DEPRECATION")
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
