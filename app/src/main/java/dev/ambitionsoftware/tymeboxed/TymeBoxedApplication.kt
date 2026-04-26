package dev.ambitionsoftware.tymeboxed

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase
import dev.ambitionsoftware.tymeboxed.di.PermissionsEntryPoint
import dev.ambitionsoftware.tymeboxed.domain.model.normalizedForBreaks
import dev.ambitionsoftware.tymeboxed.domain.model.toDomain
import dev.ambitionsoftware.tymeboxed.service.BlockingStateRestorer
import dev.ambitionsoftware.tymeboxed.service.ActiveBlockingState
import dev.ambitionsoftware.tymeboxed.service.SessionBlockerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Hilt application entry point. Mirrors the minimal iOS bootstrap in
 * `TymeBoxedApp.swift` — registers the foreground-service notification
 * channel and rehydrates any active blocking session that was running when
 * the process was killed.
 */
@HiltAndroidApp
class TymeBoxedApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createSessionNotificationChannel()
        createSessionReminderChannel()
        // Re-read permission flags after channel creation; cold start must match system state
        // before any Compose screen reads [PermissionsCoordinator].
        EntryPointAccessors.fromApplication(this, PermissionsEntryPoint::class.java)
            .permissionsCoordinator()
            .refresh()
        rehydrateBlockingState()
    }

    private fun createSessionNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            SESSION_CHANNEL_ID,
            getString(R.string.session_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.session_channel_description)
            setShowBadge(false)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    /**
     * If the app process was killed while a session was active (e.g. by the
     * system, or during a crash), rehydrate [ActiveBlockingState] so the
     * accessibility service can immediately enforce blocking when it reconnects.
     *
     * Also restarts the foreground service for the persistent notification.
     */
    private fun rehydrateBlockingState() {
        // Only rehydrate if not already blocking (e.g. HomeViewModel already did it)
        if (ActiveBlockingState.current.isBlocking) return

        appScope.launch {
            try {
                val db = TymeBoxedDatabase.getInstance(this@TymeBoxedApplication)

                val activeSession = db.sessionDao().findActive() ?: return@launch
                val profileWithApps = db.profileDao()
                    .getByIdWithApps(activeSession.profileId) ?: return@launch
                val profile = profileWithApps.toDomain().normalizedForBreaks()
                val session = activeSession.toDomain()

                val blockedPkgs = profile.blockedPackages.toSet()
                BlockingStateRestorer.apply(
                    profile = profile,
                    session = session,
                    blockedPackages = blockedPkgs,
                )

                // Restart foreground service
                val serviceIntent = SessionBlockerService.startIntent(
                    this@TymeBoxedApplication,
                    profile.name,
                    activeSession.startTime,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                android.util.Log.i(
                    "TymeBoxedApp",
                    "Rehydrated blocking for '${profile.name}' with ${blockedPkgs.size} blocked packages.",
                )
            } catch (e: Throwable) {
                android.util.Log.w(
                    "TymeBoxedApp",
                    "Failed to rehydrate blocking state: ${e.message}",
                )
            }
        }
    }

    private fun createSessionReminderChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            SESSION_REMINDER_CHANNEL_ID,
            getString(R.string.session_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.session_reminder_channel_description)
            setShowBadge(true)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val SESSION_CHANNEL_ID = "tymeboxed_session"
        const val SESSION_REMINDER_CHANNEL_ID = "tymeboxed_session_reminder"
    }
}
