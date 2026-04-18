package dev.ambitionsoftware.tymeboxed

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase
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

                val profile = profileWithApps.profile
                val blockedPkgs = profileWithApps.blockedApps
                    .map { it.packageName }.toSet()

                val domainList = profile.domains
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()

                ActiveBlockingState.activate(
                    profileId = activeSession.profileId,
                    blockedPackages = blockedPkgs,
                    isAllowMode = profile.isAllowMode,
                    domains = domainList,
                    isAllowModeDomains = profile.isAllowModeDomains,
                )

                // Restart foreground service
                val serviceIntent = SessionBlockerService.startIntent(
                    this@TymeBoxedApplication,
                    profile.name,
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

    companion object {
        const val SESSION_CHANNEL_ID = "tymeboxed_session"
    }
}
