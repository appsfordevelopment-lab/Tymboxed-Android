package dev.ambitionsoftware.tymeboxed.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dev.ambitionsoftware.tymeboxed.domain.model.Profile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import dev.ambitionsoftware.tymeboxed.R
import dev.ambitionsoftware.tymeboxed.MainActivity

/**
 * When a focus session **ends**, schedules a one-shot local notification [delaySec] in the
 * future, mirroring iOS [StrategyManager.scheduleReminder] and [TimersUtil.scheduleNotification].
 * When a new session **starts**, [cancelAll] is called (see [dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeViewModel.startSession]).
 */
@Singleton
class SessionReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Cancels a pending end-of-session reminder (iOS: [TimersUtil.cancelAll] on session start).
     */
    fun cancelAll() {
        val i = Intent(context, SessionReminderReceiver::class.java).apply {
            action = ACTION
            data = Uri.parse("tymeboxed://session_reminder")
        }
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            i,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pi)
    }

    /**
     * Schedules the reminder for [Profile.reminderTimeSeconds] from now. No-op if disabled.
     */
    fun scheduleAfterSessionEnd(profile: Profile) {
        val delaySec = profile.reminderTimeSeconds ?: return
        if (delaySec <= 0) return

        val title = context.getString(R.string.session_reminder_title, profile.name.ifBlank { context.getString(R.string.app_name) })
        val body = profile.customReminderMessage?.trim()?.takeIf { it.isNotEmpty() }
            ?: context.getString(R.string.session_reminder_default_body, profile.name.ifBlank { "…" })

        cancelAll()

        val triggerAt = System.currentTimeMillis() + delaySec * 1000L
        val show = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val operation = pendingIntentForBroadcast(title, body)
        val info = AlarmManager.AlarmClockInfo(triggerAt, show)
        alarmManager.setAlarmClock(info, operation)
        Log.i(TAG, "Session reminder in ${delaySec}s for profile ${profile.id}")
    }

    private fun pendingIntentForBroadcast(title: String?, body: String?): PendingIntent {
        val intent = Intent(context, SessionReminderReceiver::class.java).apply {
            action = ACTION
            data = Uri.parse("tymeboxed://session_reminder")
            if (title != null) putExtra(EXTRA_TITLE, title)
            if (body != null) putExtra(EXTRA_BODY, body)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val TAG = "SessionReminder"
        const val ACTION = "dev.ambitionsoftware.tymeboxed.action.SESSION_END_REMINDER"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val NOTIFICATION_ID = 0x4d52
        private const val REQUEST_CODE = 0x4001
    }
}
