package dev.ambitionsoftware.tymeboxed.service.inapp

import android.content.Context
import androidx.core.content.edit

/**
 * Global daily in-app time budget (minutes) across timed surfaces, matching Switchly
 * [InAppLimitStore](https://gitlab.com/Saltyy/switchly-public) behavior.
 */
object InAppLimitStore {
    private const val PREFS = "tymeboxed_inapp_limits"
    private const val KEY_GLOBAL_MIN = "inapp_limit_min__default"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /**
     * @return 0 = no limit (toggles block immediately), &gt;0 = daily cap in minutes
     *         across the timed surfaces in [inAppTotalUsageKeys].
     */
    fun getLimitMinutes(context: Context): Int {
        return prefs(context).getInt(KEY_GLOBAL_MIN, 0).coerceAtLeast(0)
    }

    fun setLimitMinutes(context: Context, minutes: Int) {
        if (minutes <= 0) {
            prefs(context).edit { remove(KEY_GLOBAL_MIN) }
        } else {
            prefs(context).edit { putInt(KEY_GLOBAL_MIN, minutes) }
        }
    }
}
