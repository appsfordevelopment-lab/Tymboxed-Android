package dev.ambitionsoftware.tymeboxed.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.data.db.dao.ProfileDao
import dev.ambitionsoftware.tymeboxed.domain.model.normalizedForBreaks
import dev.ambitionsoftware.tymeboxed.domain.model.toDomain
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Registers exact alarms for the next schedule start/end per profile (Android analogue of
 * iOS DeviceActivity schedule monitoring).
 */
@Singleton
class ProfileScheduleAlarmScheduler @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val profileDao: ProfileDao,
) {

    suspend fun rescheduleAll() = withContext(Dispatchers.IO) {
        val profiles = profileDao.getAllWithAppsSnapshot()
            .map { it.toDomain().normalizedForBreaks() }
        for (p in profiles) {
            cancelAlarmsForProfile(p.id)
        }
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        for (profile in profiles) {
            val sched = profile.schedule ?: continue
            if (!sched.isActive) continue
            val minStartAfter = maxOf(now, sched.updatedAt + FIFTEEN_MIN_MS)
            val nextStart = sched.nextStartAfter(minStartAfter - 1, zone)
            val nextEnd = sched.nextEndAfter(now - 1, zone)
            if (nextStart != null) {
                scheduleAlarm(profile.id, nextStart, isStart = true)
            }
            if (nextEnd != null) {
                scheduleAlarm(profile.id, nextEnd, isStart = false)
            }
        }
        Log.i(TAG, "Rescheduled alarms for ${profiles.count { it.schedule?.isActive == true }} profiles")
    }

    fun cancelAlarmsForProfile(profileId: String) {
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        am.cancel(pendingIntent(profileId, isStart = true))
        am.cancel(pendingIntent(profileId, isStart = false))
    }

    private fun scheduleAlarm(profileId: String, triggerAtMillis: Long, isStart: Boolean) {
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pi = pendingIntent(profileId, isStart)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, pi),
                    pi,
                )
            } else {
                @Suppress("DEPRECATION")
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm not permitted: ${e.message}")
        }
    }

    private fun pendingIntent(profileId: String, isStart: Boolean): PendingIntent {
        val intent = Intent(appContext, ProfileScheduleReceiver::class.java).apply {
            action = if (isStart) ProfileScheduleReceiver.ACTION_START else ProfileScheduleReceiver.ACTION_END
            putExtra(ProfileScheduleReceiver.EXTRA_PROFILE_ID, profileId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode(profileId, isStart),
            intent,
            flags,
        )
    }

    private fun requestCode(profileId: String, isStart: Boolean): Int {
        val h = profileId.hashCode()
        val salt = if (isStart) 0x3C12A5 else 0x5E71B9
        return abs(h xor salt) and 0x0FFFFFFF
    }

    companion object {
        private const val TAG = "ProfileScheduleAlarm"
        private const val FIFTEEN_MIN_MS = 15 * 60_000L
    }
}
