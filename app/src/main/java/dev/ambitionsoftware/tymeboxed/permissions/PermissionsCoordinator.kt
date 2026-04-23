package dev.ambitionsoftware.tymeboxed.permissions

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.service.AppBlockerAccessibilityService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for whether each [TymePermission] is currently granted.
 *
 * This is the Android-side analogue of iOS `RequestAuthorizer` (which only has
 * to track Family Controls). Because Android has seven distinct permissions
 * with different check APIs, all of them are centralized here and exposed as a
 * single [StateFlow] that the intro wizard, the Settings > Permissions card,
 * and [PermissionsViewModel] (home banner / session gate) subscribe to.
 *
 * Call [refresh] on app foreground (ON_RESUME) — Android system settings pages
 * don't notify us when a permission changes, so we recompute whenever the user
 * comes back from granting one.
 */
@Singleton
class PermissionsCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _states: MutableStateFlow<Map<TymePermission, Boolean>>
    private val _allRequiredGranted: MutableStateFlow<Boolean>

    init {
        val initial = computeAll()
        _states = MutableStateFlow(initial)
        _allRequiredGranted = MutableStateFlow(allRequiredFrom(initial))
    }

    /** Map of permission -> granted flag. Updates only when [refresh] is called. */
    val states: StateFlow<Map<TymePermission, Boolean>> = _states.asStateFlow()

    /**
     * Same boolean as [allRequiredFrom] for [states] value — updated in the same [refresh] call
     * so UI never sees a mismatched pair (avoids false "missing permission" flashes).
     */
    val allRequiredGranted: StateFlow<Boolean> = _allRequiredGranted.asStateFlow()

    fun refresh() {
        val map = computeAll()
        _states.value = map
        _allRequiredGranted.value = allRequiredFrom(map)
    }

    private fun allRequiredFrom(map: Map<TymePermission, Boolean>): Boolean =
        TymePermission.requiredPermissions.all { map[it] == true }

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
        if (isAccessibilityEnabledViaAccessibilityManager()) return true
        return isAccessibilityEnabledViaSettingsSecure()
    }

    /**
     * Matches what the system UI shows under Accessibility — more reliable than parsing
     * [Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES] alone (some OEMs format strings oddly).
     */
    private fun isAccessibilityEnabledViaAccessibilityManager(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val cn = ComponentName(context, AppBlockerAccessibilityService::class.java)
        val expectedFlat = cn.flattenToString()
        val simpleName = AppBlockerAccessibilityService::class.java.simpleName
        val list = try {
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        } catch (_: Throwable) {
            return false
        }
        return list.any { info ->
            val id = info.id?.trim().orEmpty()
            if (id.isNotEmpty() && id.equals(expectedFlat, ignoreCase = true)) return@any true
            runCatching {
                val parsed = ComponentName.unflattenFromString(id)
                parsed != null &&
                    parsed.packageName == cn.packageName &&
                    (
                        parsed.className == cn.className ||
                            parsed.className.endsWith(".$simpleName") ||
                            parsed.className.endsWith(simpleName)
                        )
            }.getOrDefault(false) ||
                (
                    id.contains(cn.packageName, ignoreCase = true) &&
                        id.contains(simpleName, ignoreCase = true)
                    )
        }
    }

    private fun isAccessibilityEnabledViaSettingsSecure(): Boolean {
        val cn = ComponentName(context, AppBlockerAccessibilityService::class.java)
        val expectedFlat = cn.flattenToString()
        val simpleName = AppBlockerAccessibilityService::class.java.simpleName
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        if (enabled.isBlank()) return false
        val entries = enabled.split(':', ',')
        return entries.any { raw ->
            val entry = raw.trim()
            if (entry.isEmpty()) return@any false
            if (entry.equals(expectedFlat, ignoreCase = true)) return@any true
            val parsed = ComponentName.unflattenFromString(entry)
            if (parsed != null) {
                parsed.packageName == cn.packageName && (
                    parsed.className == cn.className ||
                        parsed.className.endsWith(".$simpleName") ||
                        parsed.className.endsWith(simpleName)
                    )
            } else {
                entry.contains(cn.packageName, ignoreCase = true) &&
                    entry.contains(simpleName, ignoreCase = true)
            }
        }
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
        if (mode == AppOpsManager.MODE_ALLOWED) return true
        // OEMs sometimes leave AppOp as MODE_DEFAULT even after the user enabled Usage Access.
        return probeUsageStatsPermissionActuallyWorks()
    }

    /**
     * [AppOpsManager] can lie on some ROMs; [UsageStatsManager.queryUsageStats] throws
     * [SecurityException] when access is really denied.
     */
    private fun probeUsageStatsPermissionActuallyWorks(): Boolean {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return false
        val end = System.currentTimeMillis()
        val start = (end - 1000L * 60 * 60 * 2).coerceAtLeast(0L)
        return try {
            @Suppress("DEPRECATION")
            usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun isNotificationsGranted(): Boolean {
        val notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val post = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            // Treat as granted if either path is satisfied (fixes OEMs where one lags the other).
            return notificationsOn || post
        }
        return notificationsOn
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
