package dev.ambitionsoftware.tymeboxed.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.ambitionsoftware.tymeboxed.domain.model.ProfileSchedule
import dev.ambitionsoftware.tymeboxed.ui.theme.LocalAccentColor
import java.util.Calendar

private val weekdayDefs: List<Pair<Int, String>> = listOf(
    Calendar.SUNDAY to "Su",
    Calendar.MONDAY to "Mo",
    Calendar.TUESDAY to "Tu",
    Calendar.WEDNESDAY to "We",
    Calendar.THURSDAY to "Th",
    Calendar.FRIDAY to "Fr",
    Calendar.SATURDAY to "Sa",
)

private val minuteSteps = (0..55 step 5).toList()
private val hours12 = (1..12).toList()

/**
 * Full-screen schedule editor — mirrors iOS [SchedulePicker].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePickerScreen(
    viewModel: ProfileEditViewModel,
    onBack: () -> Unit,
) {
    val accent = LocalAccentColor.current.value
    val cs = MaterialTheme.colorScheme
    val initial = viewModel.state.value.schedule

    var selectedDays by remember {
        mutableStateOf(initial.days.toSet())
    }
    var startHour12 by remember { mutableIntStateOf(from24To12(initial.startHour).first) }
    var startPm by remember { mutableStateOf(from24To12(initial.startHour).second) }
    var startMinute by remember { mutableIntStateOf(roundToFive(initial.startMinute)) }
    var endHour12 by remember { mutableIntStateOf(from24To12(initial.endHour).first) }
    var endPm by remember { mutableStateOf(from24To12(initial.endHour).second) }
    var endMinute by remember { mutableIntStateOf(roundToFive(initial.endMinute)) }

    var showStartPickers by remember { mutableStateOf(false) }
    var showEndPickers by remember { mutableStateOf(false) }

    val start24 = hour12To24(startHour12, startPm)
    val end24 = hour12To24(endHour12, endPm)
    val startTotal = start24 * 60 + startMinute
    val endTotal = end24 * 60 + endMinute
    val durationMinutes = if (endTotal <= startTotal) {
        (24 * 60 - startTotal) + endTotal
    } else {
        endTotal - startTotal
    }
    val hasDays = selectedDays.isNotEmpty()
    val isValid = hasDays && durationMinutes >= 60
    val validationText = when {
        isValid -> null
        !hasDays -> null
        else -> "Schedule must be at least 1 hour long."
    }

    fun applyAndDone() {
        val daysSorted = selectedDays.sorted()
        val sched = ProfileSchedule(
            days = daysSorted,
            startHour = start24,
            startMinute = startMinute,
            endHour = end24,
            endMinute = endMinute,
            updatedAt = System.currentTimeMillis(),
        )
        viewModel.updateSchedule(sched)
        onBack()
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = { Text("Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { applyAndDone() },
                        enabled = isValid,
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.background,
                    titleContentColor = cs.onBackground,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose when this profile starts and ends. To end early, use the strategy you set up earlier. The schedule must be at least 1 hour long.",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Days",
                style = MaterialTheme.typography.labelLarge,
                color = cs.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                weekdayDefs.forEach { (dow, label) ->
                    val sel = dow in selectedDays
                    val bg = if (sel) accent else Color.Transparent
                    val fg = if (sel) Color.White else cs.onSurface
                    val borderC = if (sel) accent else cs.outline
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = fg,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, borderC, CircleShape)
                            .background(bg, CircleShape)
                            .clickable {
                                selectedDays = if (sel) selectedDays - dow else selectedDays + dow
                                if (selectedDays.isEmpty()) {
                                    showStartPickers = false
                                    showEndPickers = false
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }
            if (hasDays) {
                Text(
                    text = "Schedules take 15 minutes to update",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Start Time",
                style = MaterialTheme.typography.labelLarge,
                color = cs.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hasDays) {
                        showStartPickers = !showStartPickers
                        if (showStartPickers) showEndPickers = false
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("When to start", style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                Text(
                    format12hLabel(startHour12, startMinute, startPm),
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onSurfaceVariant,
                )
            }
            if (showStartPickers && hasDays) {
                DropdownTimeSelectors(
                    hour12 = startHour12,
                    onHour = { startHour12 = it },
                    minute = startMinute,
                    onMinute = { startMinute = it },
                    isPm = startPm,
                    onPm = { startPm = it },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "End Time",
                style = MaterialTheme.typography.labelLarge,
                color = cs.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hasDays) {
                        showEndPickers = !showEndPickers
                        if (showEndPickers) showStartPickers = false
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("When to end", style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                Text(
                    format12hLabel(endHour12, endMinute, endPm),
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onSurfaceVariant,
                )
            }
            if (showEndPickers && hasDays) {
                DropdownTimeSelectors(
                    hour12 = endHour12,
                    onHour = { endHour12 = it },
                    minute = endMinute,
                    onMinute = { endMinute = it },
                    isPm = endPm,
                    onPm = { endPm = it },
                )
            }
            validationText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = {
                    selectedDays = emptySet()
                    startHour12 = 9
                    startPm = false
                    startMinute = 0
                    endHour12 = 5
                    endPm = true
                    endMinute = 0
                    showStartPickers = false
                    showEndPickers = false
                    viewModel.updateSchedule(ProfileSchedule.inactive())
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Remove Schedule", color = cs.error, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DropdownTimeSelectors(
    hour12: Int,
    onHour: (Int) -> Unit,
    minute: Int,
    onMinute: (Int) -> Unit,
    isPm: Boolean,
    onPm: (Boolean) -> Unit,
) {
    var hourExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }
    var merExpanded by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TextButton(
                onClick = { hourExpanded = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Hour: $hour12", color = cs.primary)
            }
            DropdownMenu(
                expanded = hourExpanded,
                onDismissRequest = { hourExpanded = false },
            ) {
                hours12.forEach { h ->
                    DropdownMenuItem(
                        text = { Text(h.toString()) },
                        onClick = {
                            onHour(h)
                            hourExpanded = false
                        },
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            TextButton(
                onClick = { minuteExpanded = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Min: %02d".format(minute), color = cs.primary)
            }
            DropdownMenu(
                expanded = minuteExpanded,
                onDismissRequest = { minuteExpanded = false },
            ) {
                minuteSteps.forEach { m ->
                    DropdownMenuItem(
                        text = { Text("%02d".format(m)) },
                        onClick = {
                            onMinute(m)
                            minuteExpanded = false
                        },
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            TextButton(
                onClick = { merExpanded = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isPm) "PM" else "AM", color = cs.primary)
            }
            DropdownMenu(
                expanded = merExpanded,
                onDismissRequest = { merExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("AM") },
                    onClick = {
                        onPm(false)
                        merExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("PM") },
                    onClick = {
                        onPm(true)
                        merExpanded = false
                    },
                )
            }
        }
    }
}

private fun from24To12(hour24: Int): Pair<Int, Boolean> {
    val isPm = hour24 >= 12
    var h = hour24 % 12
    if (h == 0) h = 12
    return h to isPm
}

private fun hour12To24(hour12: Int, isPm: Boolean): Int {
    if (hour12 == 12) return if (isPm) 12 else 0
    return if (isPm) hour12 + 12 else hour12
}

private fun roundToFive(value: Int): Int {
    val rem = value % 5
    val down = value - rem
    val up = (value + (5 - rem)).coerceAtMost(55)
    if (rem == 0) return value
    return if (value - down < up - value) down else up
}

private fun format12hLabel(h12: Int, minute: Int, pm: Boolean): String =
    "$h12:${"%02d".format(minute)} ${if (pm) "PM" else "AM"}"
