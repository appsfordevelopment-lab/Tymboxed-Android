package dev.ambitionsoftware.tymeboxed.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dev.ambitionsoftware.tymeboxed.data.db.TymeBoxedDatabase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Restores an active blocking session after device reboot.
 *
 * On BOOT_COMPLETED / LOCKED_BOOT_COMPLETED, queries Room for any session
 * whose endTime is null. If one exists, rehydrates [ActiveBlockingState]
 * and restarts [SessionBlockerService] so blocking survives a reboot.
 *
 * Uses a short-lived CoroutineScope for the DB query since BroadcastReceiver
 * has a ~10 second execution window.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var database: TymeBoxedDatabase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        Log.i(TAG, "Boot completed — checking for active session to restore.")

        val pendingResult = goAsync()

        scope.launch {
            try {
                // Find any active (non-ended) session
                val activeSession = database.sessionDao().findActive()
                if (activeSession == null) {
                    Log.i(TAG, "No active session to restore.")
                    pendingResult.finish()
                    return@launch
                }

                // Load the profile for this session
                val profileWithApps = database.profileDao().getByIdWithApps(activeSession.profileId)
                if (profileWithApps == null) {
                    Log.w(TAG, "Active session found but profile not found. Ending stale session.")
                    database.sessionDao().endAllActive(System.currentTimeMillis())
                    pendingResult.finish()
                    return@launch
                }

                val profile = profileWithApps.profile
                val blockedPkgs = profileWithApps.blockedApps.map { it.packageName }.toSet()

                // Rehydrate blocking state
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

                // Restart the foreground service
                val serviceIntent = SessionBlockerService.startIntent(
                    context.applicationContext,
                    profile.name,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.applicationContext.startForegroundService(serviceIntent)
                } else {
                    context.applicationContext.startService(serviceIntent)
                }

                Log.i(TAG, "Restored active session for profile '${profile.name}' " +
                    "with ${blockedPkgs.size} blocked packages.")
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to restore session on boot: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
