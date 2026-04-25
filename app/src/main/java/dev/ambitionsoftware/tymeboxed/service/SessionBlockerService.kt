package dev.ambitionsoftware.tymeboxed.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.EntryPointAccessors
import dev.ambitionsoftware.tymeboxed.MainActivity
import dev.ambitionsoftware.tymeboxed.R
import dev.ambitionsoftware.tymeboxed.TymeBoxedApplication
import dev.ambitionsoftware.tymeboxed.di.ServiceBridgeEntryPoint
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Foreground service that keeps Android from killing the blocking engine
 * while a focus session is active. Shows a persistent notification with
 * a "Tyme Boxed is blocking" message and a tap-to-open action.
 *
 * Started by [HomeViewModel] when a session begins; stopped when it ends.
 * The AccessibilityService does the actual blocking — this service exists
 * purely to satisfy Android's foreground-service requirement so the process
 * stays alive in the background.
 */
class SessionBlockerService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)
    private var tickJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        // Channel is already created in TymeBoxedApplication.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "Stop action received — deactivating blocking.")
                tickJob?.cancel()
                tickJob = null
                ActiveBlockingState.deactivate()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // After process death, rehydration passes the name on the intent. If the system
        // restarts this START_STICKY service with a null intent, Extras are missing — use
        // in-memory [ActiveBlockingState] which is set whenever blocking activates.
        val profileName = intent?.getStringExtra(EXTRA_PROFILE_NAME)
            .takeUnless { it.isNullOrBlank() }
            ?: ActiveBlockingState.current.profileName
            .takeUnless { it.isNullOrBlank() }
            ?: "Focus Session"
        val sessionStartMs = intent?.getLongExtra(EXTRA_SESSION_START_MS, 0L)?.takeIf { it > 0L }
            ?: ActiveBlockingState.current.sessionStartTimeMs.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        Log.i(TAG, "SessionBlockerService started for profile: $profileName (start=$sessionStartMs)")

        val notification = buildNotification(profileName, sessionStartMs)
        startForeground(NOTIFICATION_ID, notification)
        startTimerTickLoop()

        return START_STICKY
    }

    private fun startTimerTickLoop() {
        tickJob?.cancel()
        val entry = EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceBridgeEntryPoint::class.java,
        )
        val handler = entry.sessionTimerHandler()
        tickJob = serviceScope.launch {
            while (isActive) {
                delay(1_000L)
                try {
                    withContext(Dispatchers.IO) {
                        handler.onServiceTick()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Session timer tick: ${e.message}")
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        tickJob?.cancel()
        serviceJob.cancel()
        super.onDestroy()
        Log.i(TAG, "SessionBlockerService destroyed.")
    }

    private fun buildNotification(profileName: String, sessionStartTimeMs: Long): Notification {
        // Tap opens the app
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tyme Boxed active")
            .setContentText("Blocking for \"$profileName\" — tap to open, stop from the app.")
            // Live elapsed time (system updates the chronometer from session start)
            .setWhen(sessionStartTimeMs)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(openPending)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val TAG = "SessionBlockerSvc"
        private const val CHANNEL_ID = TymeBoxedApplication.SESSION_CHANNEL_ID
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "dev.ambitionsoftware.tymeboxed.STOP_SESSION"
        const val EXTRA_PROFILE_NAME = "profile_name"
        const val EXTRA_SESSION_START_MS = "session_start_ms"

        fun startIntent(context: Context, profileName: String, sessionStartTimeMs: Long): Intent =
            Intent(context, SessionBlockerService::class.java).apply {
                putExtra(EXTRA_PROFILE_NAME, profileName)
                putExtra(EXTRA_SESSION_START_MS, sessionStartTimeMs)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, SessionBlockerService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
