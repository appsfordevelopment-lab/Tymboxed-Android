package dev.ambitionsoftware.tymeboxed.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository
import dev.ambitionsoftware.tymeboxed.domain.model.Session
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class InsightsPeriod {
    THIS_WEEK,
    THIS_MONTH,
}

data class ProfileInsightsUiState(
    val period: InsightsPeriod = InsightsPeriod.THIS_WEEK,
    val periodButtonLabel: String = "This week",
    val avgFocusMinutes: Int = 0,
    /** 7 values: week = each calendar day; month = sum of focus minutes per day-of-week (Sun–Sat). */
    val dailyFocusMinutes: List<Int> = List(7) { 0 },
    val dayLabels: List<String> = emptyList(),
    val totalFocusMinutes: Int = 0,
    val completedSessionCount: Int = 0,
)

@HiltViewModel
class ProfileInsightsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileInsightsUiState())
    val uiState: StateFlow<ProfileInsightsUiState> = _state.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(InsightsPeriod.THIS_WEEK)
    val selectedPeriod: StateFlow<InsightsPeriod> = _selectedPeriod.asStateFlow()

    private var collectJob: Job? = null
    private var currentProfileId: String? = null

    fun startCollecting(profileId: String) {
        currentProfileId = profileId
        startCollectingInternal()
    }

    fun setPeriod(period: InsightsPeriod) {
        if (_selectedPeriod.value == period) return
        _selectedPeriod.value = period
        if (currentProfileId != null) {
            startCollectingInternal()
        }
    }

    fun stopCollecting() {
        collectJob?.cancel()
        collectJob = null
    }

    private fun startCollectingInternal() {
        val profileId = currentProfileId ?: return
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val period = _selectedPeriod.value
            when (period) {
                InsightsPeriod.THIS_WEEK -> collectWeek(profileId, zone)
                InsightsPeriod.THIS_MONTH -> collectMonth(profileId, zone)
            }
        }
    }

    private suspend fun collectWeek(profileId: String, zone: ZoneId) {
        val weekStart = ZonedDateTime.now(zone).toLocalDate()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val startMs = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = weekStart.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
        val labelFormatter = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())
        val labels = (0 until 7).map { o ->
            weekStart.plusDays(o.toLong()).format(labelFormatter)
        }
        _state.value = ProfileInsightsUiState(
            period = InsightsPeriod.THIS_WEEK,
            periodButtonLabel = "This week",
            dayLabels = labels,
        )
        sessionRepository.observeCompletedSessionsForProfileBetween(profileId, startMs, endMs)
            .collect { sessions ->
                _state.value = buildWeekState(
                    sessions = sessions,
                    weekStart = weekStart,
                    zone = zone,
                    labels = labels,
                )
            }
    }

    private suspend fun collectMonth(profileId: String, zone: ZoneId) {
        val ym = YearMonth.from(ZonedDateTime.now(zone).toLocalDate())
        val firstDay = ym.atDay(1)
        val startMs = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        // Sun = index 0 … Sat = 6, matching [DayOfWeek.SUNDAY]…[SATURDAY]
        val dayNames = listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
        ).map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
        _state.value = ProfileInsightsUiState(
            period = InsightsPeriod.THIS_MONTH,
            periodButtonLabel = "This month",
            dayLabels = dayNames,
        )
        sessionRepository.observeCompletedSessionsForProfileBetween(profileId, startMs, endMs)
            .collect { sessions ->
                _state.value = buildMonthState(
                    sessions = sessions,
                    yearMonth = ym,
                    zone = zone,
                    dayLabels = dayNames,
                )
            }
    }

    private fun buildWeekState(
        sessions: List<Session>,
        weekStart: LocalDate,
        zone: ZoneId,
        labels: List<String>,
    ): ProfileInsightsUiState {
        val daily = MutableList(7) { 0 }
        var total = 0
        for (s in sessions) {
            val end = s.endTime ?: continue
            if (end <= s.startTime) continue
            val durMin = ((end - s.startTime) / 60_000L).toInt().coerceAtLeast(0)
            total += durMin
            val localDate = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
            val dayIndex = ChronoUnit.DAYS.between(weekStart, localDate).toInt()
            if (dayIndex in 0..6) {
                daily[dayIndex] += durMin
            }
        }
        val n = sessions.size
        val avg = if (n == 0) 0 else (total.toFloat() / n).roundToInt()
        return ProfileInsightsUiState(
            period = InsightsPeriod.THIS_WEEK,
            periodButtonLabel = "This week",
            avgFocusMinutes = avg,
            dailyFocusMinutes = daily.toList(),
            dayLabels = labels,
            totalFocusMinutes = total,
            completedSessionCount = n,
        )
    }

    private fun buildMonthState(
        sessions: List<Session>,
        yearMonth: YearMonth,
        zone: ZoneId,
        dayLabels: List<String>,
    ): ProfileInsightsUiState {
        val byDow = MutableList(7) { 0 }
        var total = 0
        for (s in sessions) {
            val end = s.endTime ?: continue
            if (end <= s.startTime) continue
            val localDate = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
            if (YearMonth.from(localDate) != yearMonth) continue
            val durMin = ((end - s.startTime) / 60_000L).toInt().coerceAtLeast(0)
            total += durMin
            // Sun=0 … Sat=6 (dayOfWeek: Sun=7 → 7%7=0, Mon=1, …)
            val idx = localDate.dayOfWeek.value % 7
            if (idx in 0..6) {
                byDow[idx] += durMin
            }
        }
        val n = sessions.size
        val avg = if (n == 0) 0 else (total.toFloat() / n).roundToInt()
        return ProfileInsightsUiState(
            period = InsightsPeriod.THIS_MONTH,
            periodButtonLabel = "This month",
            avgFocusMinutes = avg,
            dailyFocusMinutes = byDow.toList(),
            dayLabels = dayLabels,
            totalFocusMinutes = total,
            completedSessionCount = n,
        )
    }
}
