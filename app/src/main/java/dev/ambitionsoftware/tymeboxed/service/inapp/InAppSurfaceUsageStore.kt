package dev.ambitionsoftware.tymeboxed.service.inapp

import android.content.Context
import androidx.core.content.edit
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

/**
 * Per-surface daily usage (yt:shorts, ig:reels, …) — same model as Switchly
 * [SurfaceUsageStore](https://gitlab.com/Saltyy/switchly-public).
 */
object InAppSurfaceUsageStore {
    private const val PREFS = "tymeboxed_inapp_surface_usage"
    private const val PREFIX = "surf_usage_day_"
    private const val FLUSH_MS = 10_000L
    private const val MAX_PENDING = 32

    private val lock = Any()
    private val pending = ConcurrentHashMap<String, Long>()

    @Volatile
    private var lastFlush: Long = 0L

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun dayKey(): String {
        val cal = Calendar.getInstance()
        return "%04d%02d%02d".format(
            cal[Calendar.YEAR],
            cal[Calendar.MONTH] + 1,
            cal[Calendar.DAY_OF_MONTH],
        )
    }

    private fun key(surfaceKey: String) = "${PREFIX}${dayKey()}_$surfaceKey"

    fun addUsageMsToday(context: Context, surfaceKey: String, deltaMs: Long) {
        if (surfaceKey.isBlank() || deltaMs <= 0) return
        val k = key(surfaceKey)
        pending.merge(k, deltaMs) { a, b -> a + b }
        maybeFlush(context, false)
    }

    fun getUsageMsToday(context: Context, surfaceKey: String): Long {
        if (surfaceKey.isBlank()) return 0L
        val k = key(surfaceKey)
        val p = prefs(context)
        return (p.getLong(k, 0L) + (pending[k] ?: 0L)).coerceAtLeast(0L)
    }

    fun flush(context: Context) = maybeFlush(context, true)

    private fun maybeFlush(context: Context, force: Boolean) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastFlush) < FLUSH_MS && pending.size < MAX_PENDING) return
        synchronized(lock) {
            if (!force && (now - lastFlush) < FLUSH_MS && pending.size < MAX_PENDING) return
            lastFlush = now
            if (pending.isEmpty()) return
            val snap = HashMap(pending)
            pending.clear()
            val p = prefs(context)
            p.edit {
                for ((k, d) in snap) {
                    putLong(k, p.getLong(k, 0L) + d)
                }
            }
        }
    }
}
