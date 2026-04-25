package dev.ambitionsoftware.tymeboxed.service

import dev.ambitionsoftware.tymeboxed.domain.model.BlockingStrategyId
import dev.ambitionsoftware.tymeboxed.domain.model.Profile
import dev.ambitionsoftware.tymeboxed.domain.model.Session

/**
 * Pushes [Session] + [Profile] from Room into [ActiveBlockingState] after
 * process death or when the app cold-starts with an open session.
 */
object BlockingStateRestorer {

    fun apply(profile: Profile, session: Session, blockedPackages: Set<String>) {
        val strategyId = profile.strategyId
        val focusEnd = if (strategyId == BlockingStrategyId.FOCUS_TIMER && !session.isPauseActive) {
            val mins = profile.strategyData?.toIntOrNull() ?: 25
            session.startTime + mins * 60_000L
        } else {
            null
        }
        val breakResume = if (strategyId == BlockingStrategyId.FOCUS_TIMER_BREAK &&
            session.isPauseActive &&
            session.pauseStartTime != null
        ) {
            val mins = profile.strategyData?.toIntOrNull() ?: 15
            session.pauseStartTime + mins * 60_000L
        } else {
            null
        }
        ActiveBlockingState.activate(
            profileId = profile.id,
            profileName = profile.name,
            blockedPackages = blockedPackages,
            isAllowMode = profile.isAllowMode,
            domains = profile.domains,
            isAllowModeDomains = profile.isAllowModeDomains,
            sessionStartTimeMs = session.startTime,
            strategyId = strategyId,
            isPauseActive = session.isPauseActive,
            breakAutoResumeAtMs = breakResume,
            focusTimerEndMs = focusEnd,
            strictModeEnabled = profile.enableStrictMode,
        )
    }
}
