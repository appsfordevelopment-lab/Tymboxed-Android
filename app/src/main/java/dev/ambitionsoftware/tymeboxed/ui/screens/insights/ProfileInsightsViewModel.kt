package dev.ambitionsoftware.tymeboxed.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository
import dev.ambitionsoftware.tymeboxed.domain.model.Session
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileInsightsUiState(
    val avgFocusMinutes: Int = 0,
    val dailyFocusMinutes: List<Int> = List(7) { 0 },
    val dayLabels: List<String> = emptyList(),
    val totalFocusMinutes: Int = 0,
    val totalBreakMinutes: Int = 0,
)

@HiltViewModel
class ProfileInsightsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileInsightsUiState())
    val uiState: StateFlow<ProfileInsightsUiState> = _state.asStateFlow()

    private var collectJob: Job? = null

    fun startCollecting(profileId: String) {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val weekStart = ZonedDateTime.now(zone).toLocalDate()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY))
            val startMs = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
            val endMs = weekStart.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
            val labelFormatter = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())
            val labels = (0 until 7).map { offset ->
                weekStart.plusDays(offset.toLong()).format(labelFormatter)
            }
            _state.value = ProfileInsightsUiState(dayLabels = labels)

            sessionRepository.observeCompletedSessionsForProfileBetween(
                profileId,
                startMs,
                endMs,
            ).collect { sessions ->
                _state.value = buildState(sessions, weekStart, zone, labels)
            }
        }
    }

    fun stopCollecting() {
        collectJob?.cancel()
        collectJob = null
    }

    private fun buildState(
        sessions: List<Session>,
        weekStart: LocalDate,
        zone: ZoneId,
        labels: List<String>,
    ): ProfileInsightsUiState {
        val daily = MutableList(7) { 0 }
        var totalFocusMin = 0
        for (s in sessions) {
            val end = s.endTime ?: continue
            val durMin = ((end - s.startTime) / 60_000L).toInt().coerceAtLeast(0)
            totalFocusMin += durMin
            val localDate = Instant.ofEpochMilli(s.startTime).atZone(zone).toLocalDate()
            val dayIndex = ChronoUnit.DAYS.between(weekStart, localDate).toInt()
            if (dayIndex in 0..6) {
                daily[dayIndex] += durMin
            }
        }
        val avg = if (sessions.isEmpty()) 0 else totalFocusMin / sessions.size
        return ProfileInsightsUiState(
            avgFocusMinutes = avg,
            dailyFocusMinutes = daily.toList(),
            dayLabels = labels,
            totalFocusMinutes = totalFocusMin,
            totalBreakMinutes = 0,
        )
    }
}
