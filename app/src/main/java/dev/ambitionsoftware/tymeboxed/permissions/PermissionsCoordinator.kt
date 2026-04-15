package dev.ambitionsoftware.tymeboxed.permissions

import android.Manifest
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.service.AppBlockerAccessibilityService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for whether each [TymePermission] is currently granted.
 *
 * This is the Android-side analogue of iOS `RequestAuthorizer` (which only has
 * to track Family Controls). Because Android has seven distinct permissions
 * with different check APIs, all of them are centralized here and exposed as a
 * single [StateFlow] that the intro wizard, the Settings > Permissions card,
 * and the "start session" gate all subscribe to.
 *
 * Call [refresh] on app foreground (ON_RESUME) — Android system settings pages
 * don't notify us when a permission changes, so we recompute whenever the user
 * comes back from granting one.
 */
@Singleton
class PermissionsCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _states = MutableStateFlow(computeAll())

    /** Map of permission -> granted flag. Updates only when [refresh] is called. */
    val states: StateFlow<Map<TymePermission, Boolean>> = _states.asStateFlow()

    /** True when every required permission is granted. Drives the "Continue" button. */
    val allRequiredGranted = states.map { map ->
        TymePermission.requiredPermissions.all { map[it] == true }
    }

    fun refresh() {
        _states.value = computeAll()
    }

    fun isGranted(perm: TymePermission): Boolean = computeOne(perm)

    private fun computeAll(): Map<TymePermission, Boolean> =
        TymePermission.entries.associateWith { computeOne(it) }

    private fun computeOne(perm: TymePermission): Boolean = when (perm) {
        TymePermission.ACCESSIBILITY -> isAccessibilityServiceEnabled()
        TymePermission.USAGE_STATS -> isUsageStatsGranted()
        TymePermission.OVERLAY -> Settings.canDrawOverlays(context)
        TymePermission.NOTIFICATIONS -> isNotificationsGranted()
        TymePermission.EXACT_ALARMS -> isExactAlarmsGranted()
        TymePermission.BATTERY_OPTIMIZATIONS -> isBatteryOptimizationsIgnored()
        TymePermission.NFC -> isNfcEnabled()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(
            context,
            AppBlockerAccessibilityService::class.java,
        ).flattenToString()
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        // System stores component names in a ':'-separated list.
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    private fun isUsageStatsGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isNotificationsGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    private fun isExactAlarmsGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                .canScheduleExactAlarms()
        } else {
            true
        }

    private fun isBatteryOptimizationsIgnored(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** True when the device has NFC hardware. False on emulators without NFC. */
    val isNfcHardwareAvailable: Boolean
        get() = NfcAdapter.getDefaultAdapter(context) != null

    private fun isNfcEnabled(): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context) ?: return false
        return adapter.isEnabled
    }
}
