package dev.ambitionsoftware.tymeboxed.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dev.ambitionsoftware.tymeboxed.MainActivity
import dev.ambitionsoftware.tymeboxed.R
import dev.ambitionsoftware.tymeboxed.TymeBoxedApplication

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

    override fun onCreate() {
        super.onCreate()
        // Channel is already created in TymeBoxedApplication.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "Stop action received — deactivating blocking.")
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
        Log.i(TAG, "SessionBlockerService started for profile: $profileName")

        val notification = buildNotification(profileName)
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "SessionBlockerService destroyed.")
    }

    private fun buildNotification(profileName: String): Notification {
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
            .setContentTitle("Tyme Boxed Active")
            .setContentText(
                "Blocking apps for \"$profileName\". Open the app and scan to stop.",
            )
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

        fun startIntent(context: Context, profileName: String): Intent =
            Intent(context, SessionBlockerService::class.java).apply {
                putExtra(EXTRA_PROFILE_NAME, profileName)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, SessionBlockerService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
