package dev.ambitionsoftware.tymeboxed.domain.model

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.TimeZone

/**
 * Weekly time window for automatic focus sessions — mirrors iOS [BlockedProfileSchedule].
 * [days] uses [Calendar.DAY_OF_WEEK]: [Calendar.SUNDAY] = 1 … [Calendar.SATURDAY] = 7.
 */
data class ProfileSchedule(
    val days: List<Int> = emptyList(),
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
) {
    private val daysSet: Set<Int> get() = days.toSet()

    val isActive: Boolean get() = days.isNotEmpty()

    val startTotalMinutes: Int get() = startHour * 60 + startMinute
    val endTotalMinutes: Int get() = endHour * 60 + endMinute

    /** Same-day window when end is after start; otherwise crosses midnight. */
    private val isOvernight: Boolean get() = endTotalMinutes <= startTotalMinutes

    val durationMinutes: Int
        get() = if (isOvernight) {
            (24 * 60 - startTotalMinutes) + endTotalMinutes
        } else {
            endTotalMinutes - startTotalMinutes
        }

    fun isValidForSave(): Boolean = isActive && durationMinutes >= 60

    fun summaryText(): String {
        if (!isActive) return "No Schedule Set"
        val short = days
            .distinct()
            .sorted()
            .mapNotNull { dow -> weekdayShortLabel(dow) }
            .joinToString(" ")
        val start = format12h(startHour, startMinute)
        val end = format12h(endHour, endMinute)
        return "$short · $start - $end"
    }

    fun olderThan15Minutes(now: Long = System.currentTimeMillis()): Boolean =
        now - updatedAt > 15 * 60_000L

    /**
     * Whether [instantMs] falls inside the configured window (local timezone).
     */
    fun isWithinWindow(instantMs: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean {
        if (!isActive) return false
        val tz = TimeZone.getTimeZone(zone)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = instantMs }
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMin = startTotalMinutes
        val endMin = endTotalMinutes
        if (!isOvernight) {
            if (dow !in daysSet) return false
            return nowMin >= startMin && nowMin < endMin
        }
        if (nowMin >= startMin) return dow in daysSet
        cal.add(Calendar.DAY_OF_MONTH, -1)
        val prevDow = cal.get(Calendar.DAY_OF_WEEK)
        return prevDow in daysSet && nowMin < endMin
    }

    /**
     * Next window start instant strictly after [afterMillis] (local calendar semantics).
     */
    fun nextStartAfter(afterMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long? {
        if (!isActive) return null
        var best: Long? = null
        val anchor = LocalDate.ofInstant(Instant.ofEpochMilli(afterMillis), zone)
        for (i in 0..14) {
            val d = anchor.plusDays(i.toLong())
            val dow = localDateToCalendarDow(d, zone)
            if (dow !in daysSet) continue
            val t = ZonedDateTime.of(d, LocalTime.of(startHour, startMinute), zone)
                .toInstant().toEpochMilli()
            if (t > afterMillis) {
                best = if (best == null) t else minOf(best, t)
            }
        }
        return best
    }

    /**
     * Next window end instant strictly after [afterMillis].
     */
    fun nextEndAfter(afterMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long? {
        if (!isActive) return null
        var best: Long? = null
        val anchor = LocalDate.ofInstant(Instant.ofEpochMilli(afterMillis), zone)
        for (i in 0..14) {
            val d = anchor.plusDays(i.toLong())
            if (!isOvernight) {
                val dow = localDateToCalendarDow(d, zone)
                if (dow !in daysSet) continue
                val t = ZonedDateTime.of(d, LocalTime.of(endHour, endMinute), zone)
                    .toInstant().toEpochMilli()
                if (t > afterMillis) {
                    best = if (best == null) t else minOf(best, t)
                }
            } else {
                val prev = d.minusDays(1)
                val prevDow = localDateToCalendarDow(prev, zone)
                if (prevDow !in daysSet) continue
                val t = ZonedDateTime.of(d, LocalTime.of(endHour, endMinute), zone)
                    .toInstant().toEpochMilli()
                if (t > afterMillis) {
                    best = if (best == null) t else minOf(best, t)
                }
            }
        }
        return best
    }

    fun toJson(): String = Companion.scheduleGson.toJson(this)

    companion object {
        fun inactive(): ProfileSchedule = ProfileSchedule()

        private val scheduleGson = Gson()

        fun fromJson(json: String?): ProfileSchedule? {
            if (json.isNullOrBlank()) return null
            return try {
                scheduleGson.fromJson(json, ProfileSchedule::class.java)
            } catch (_: JsonSyntaxException) {
                null
            }
        }
    }
}

fun String?.decodeProfileSchedule(): ProfileSchedule? = ProfileSchedule.fromJson(this)

private fun weekdayShortLabel(dow: Int): String? = when (dow) {
    Calendar.SUNDAY -> "Su"
    Calendar.MONDAY -> "Mo"
    Calendar.TUESDAY -> "Tu"
    Calendar.WEDNESDAY -> "We"
    Calendar.THURSDAY -> "Th"
    Calendar.FRIDAY -> "Fr"
    Calendar.SATURDAY -> "Sa"
    else -> null
}

private fun format12h(hour24: Int, minute: Int): String {
    var h = hour24 % 12
    if (h == 0) h = 12
    val pm = hour24 >= 12
    return "%d:%02d %s".format(h, minute, if (pm) "PM" else "AM")
}

private fun localDateToCalendarDow(date: LocalDate, zone: ZoneId): Int {
    val zdt = date.atStartOfDay(zone)
    val cal = Calendar.getInstance(TimeZone.getTimeZone(zone))
    cal.timeInMillis = zdt.toInstant().toEpochMilli()
    return cal.get(Calendar.DAY_OF_WEEK)
}
