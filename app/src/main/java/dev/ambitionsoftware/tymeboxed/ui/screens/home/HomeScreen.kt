@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package dev.ambitionsoftware.tymeboxed.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.os.Build
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.data.prefs.ActivityChartType
import dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository
import dev.ambitionsoftware.tymeboxed.domain.model.Profile
import dev.ambitionsoftware.tymeboxed.domain.model.Session
import dev.ambitionsoftware.tymeboxed.domain.model.strategyInfoById
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel
import dev.ambitionsoftware.tymeboxed.service.ActiveBlockingState
import dev.ambitionsoftware.tymeboxed.service.SessionBlockerService
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCard
import dev.ambitionsoftware.tymeboxed.ui.screens.insights.ProfileInsightsScreen
import dev.ambitionsoftware.tymeboxed.ui.theme.LocalAccentColor
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    val profiles: StateFlow<List<Profile>> = profileRepository.observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val activeSession: StateFlow<Session?> = sessionRepository.activeSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _sessionCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val sessionCounts: StateFlow<Map<String, Int>> = _sessionCounts.asStateFlow()

    val activityChartVisible: StateFlow<Boolean> = appPreferences.activityChartVisible.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val activityChartType: StateFlow<String> = appPreferences.activityChartType.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivityChartType.FOUR_WEEK,
    )

    fun setActivityChartVisible(visible: Boolean) {
        viewModelScope.launch { appPreferences.setActivityChartVisible(visible) }
    }

    fun setActivityChartType(type: String) {
        viewModelScope.launch { appPreferences.setActivityChartType(type) }
    }

    init {
        viewModelScope.launch {
            profileRepository.observeAll().collect { list ->
                _sessionCounts.value = list.associate { profile ->
                    profile.id to sessionRepository.countCompletedForProfile(profile.id)
                }
            }
        }
        // Rehydrate blocking state if the app was killed while a session was active.
        viewModelScope.launch {
            val session = sessionRepository.findActive() ?: return@launch
            if (ActiveBlockingState.current.isBlocking) return@launch
            val profile = profileRepository.findById(session.profileId) ?: return@launch
            ActiveBlockingState.activate(
                profileId = session.profileId,
                blockedPackages = profile.blockedPackages.toSet(),
                isAllowMode = profile.isAllowMode,
                domains = profile.domains,
                isAllowModeDomains = profile.isAllowModeDomains,
            )
            val serviceIntent = SessionBlockerService.startIntent(appContext, profile.name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(serviceIntent)
            } else {
                appContext.startService(serviceIntent)
            }
        }
    }

    fun startSession(profileId: String) {
        viewModelScope.launch {
            // Load the profile so we know what to block
            val profile = profileRepository.findById(profileId) ?: return@launch

            // End any lingering session first
            sessionRepository.resetActive()

            // Create the session record
            val session = Session(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                startTime = System.currentTimeMillis(),
            )
            sessionRepository.insert(session)

            // Activate blocking in-memory for the AccessibilityService
            ActiveBlockingState.activate(
                profileId = profileId,
                blockedPackages = profile.blockedPackages.toSet(),
                isAllowMode = profile.isAllowMode,
                domains = profile.domains,
                isAllowModeDomains = profile.isAllowModeDomains,
            )

            // Start foreground service (keeps the process alive + shows notification)
            val serviceIntent = SessionBlockerService.startIntent(appContext, profile.name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(serviceIntent)
            } else {
                appContext.startService(serviceIntent)
            }
        }
    }

    fun stopSession() {
        viewModelScope.launch {
            // Clear blocking state
            ActiveBlockingState.deactivate()

            // End the DB session
            sessionRepository.resetActive()

            // Stop foreground service
            appContext.startService(SessionBlockerService.stopIntent(appContext))
        }
    }
}

/**
 * Home screen — Phase 1 skeleton.
 *
 * Layout mirrors iOS `HomeView.swift:68-107`:
 *   - Top row: `AppTitle` ("Tyme Boxed") on the left + settings gear on the right.
 *   - Body: permissions banner if any required permission is missing; then
 *     profile list or an empty-state card.
 *   - FAB (+): navigates to the profile-edit route (placeholder for now).
 *
 * Phase 2 will replace the empty-state card with real `ProfileCard`
 * composables and wire the "Start Session" flow. Phase 3 adds the active-session
 * surface with live timer.
 */
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onCreateProfile: () -> Unit,
    onEditProfile: (String) -> Unit = {},
) {
    val vm: HomeViewModel = hiltViewModel()
    val permissionsVm: PermissionsViewModel = hiltViewModel()
    val profiles by vm.profiles.collectAsState()
    val activeSession by vm.activeSession.collectAsState()
    val sessionCounts by vm.sessionCounts.collectAsState()
    val activityChartVisible by vm.activityChartVisible.collectAsState()
    val activityChartType by vm.activityChartType.collectAsState()
    val allGranted by permissionsVm.allRequiredGranted.collectAsState()

    var showManageChartSheet by remember { mutableStateOf(false) }
    var showProfilesSheet by remember { mutableStateOf(false) }
    var insightsProfile by remember { mutableStateOf<Profile?>(null) }

    // Re-check permissions whenever Home regains focus (e.g. user came back
    // from Android Settings after tapping the banner).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionsVm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cs = MaterialTheme.colorScheme
    val homeBg = cs.background

    if (showManageChartSheet) {
        ManageChartBottomSheet(
            showChart = activityChartVisible,
            onShowChartChange = vm::setActivityChartVisible,
            chartType = activityChartType,
            onChartTypeChange = vm::setActivityChartType,
            onDismiss = { showManageChartSheet = false },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = homeBg,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateProfile,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create profile")
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(homeBg)
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tyme Boxed",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = cs.onBackground,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, cs.outline.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = cs.onBackground,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                if (!allGranted) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SettingsCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "Permissions required",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Some required permissions are missing. Open Settings → Permissions to grant them before starting a session.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (profiles.isEmpty()) {
                        WelcomeEmptyCard(onTap = onCreateProfile)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            ActivitySection(
                                showChart = activityChartVisible,
                                onManage = { showManageChartSheet = true },
                            )
                            ProfileRegionHeader(onManage = { showProfilesSheet = true })
                            ProfileList(
                                profiles = profiles,
                                activeSession = activeSession,
                                sessionCounts = sessionCounts,
                                onEditProfile = onEditProfile,
                                onInsightsProfile = { insightsProfile = it },
                                onStartSession = { vm.startSession(it) },
                                onStopSession = { vm.stopSession() },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // room for FAB
            }
        }

        if (showProfilesSheet) {
            ProfilesManageFullScreen(
                profiles = profiles,
                onDismiss = { showProfilesSheet = false },
                onCreateProfile = {
                    showProfilesSheet = false
                    onCreateProfile()
                },
                onEditProfile = { id ->
                    showProfilesSheet = false
                    onEditProfile(id)
                },
            )
        }

        insightsProfile?.let { profile ->
            ProfileInsightsScreen(
                profile = profile,
                onDismiss = { insightsProfile = null },
            )
        }
    }
}

/**
 * Empty-state hero card — visual twin of iOS `Welcome.swift` (hourglass chip,
 * theme blob, “Welcome to Tyme Boxed” copy). Tapping opens profile creation.
 */
@Composable
private fun WelcomeEmptyCard(onTap: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val cardShape = RoundedCornerShape(28.dp)
    val hourglassGold = Color(0xFFC4A77D)
    val titleColor = if (isDark) Color.White else cs.onSurface
    val captionColor = if (isDark) Color.White.copy(alpha = 0.92f) else cs.onSurface.copy(alpha = 0.88f)
    val subtitleColor = if (isDark) Color(0xFFBBBBBB) else cs.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .clip(cardShape)
            .clickable(onClick = onTap),
    ) {
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            if (isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E1E22),
                                    Color(0xFF252420),
                                    Color(0xFF2A2520),
                                ),
                                start = Offset(0f, h * 0.5f),
                                end = Offset(w, 0f),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE8D4B8).copy(alpha = 0.42f),
                                    Color(0xFFC4A77D).copy(alpha = 0.12f),
                                    Color.Transparent,
                                ),
                                center = Offset(w * 0.92f, h * 0.08f),
                                radius = w * 0.65f,
                            ),
                        ),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF8F8FA),
                                    Color(0xFFF2F0EC),
                                    Color(0xFFEDE8E0),
                                ),
                                start = Offset(0f, h * 0.5f),
                                end = Offset(w, 0f),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFC4A77D).copy(alpha = 0.28f),
                                    Color(0xFFE8D4B8).copy(alpha = 0.18f),
                                    Color.Transparent,
                                ),
                                center = Offset(w * 0.92f, h * 0.08f),
                                radius = w * 0.65f,
                            ),
                        ),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Physically block distracting apps",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = captionColor,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) hourglassGold else hourglassGold.copy(alpha = 0.95f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassEmpty,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                fontSize = 26.sp,
            )
            Text(
                text = "Tyme Boxed",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tap here to get started on your first profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun ActivitySection(
    showChart: Boolean,
    onManage: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = cs.onSurfaceVariant,
            )
            ManagePill(
                icon = Icons.Default.ShowChart,
                onClick = onManage,
            )
        }
        if (showChart) {
            ActivityHeatmapCard()
        } else {
            ActivityChartHiddenPlaceholder()
        }
    }
}

@Composable
private fun ActivityChartHiddenPlaceholder() {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clip(shape)
            .border(1.dp, cs.outline.copy(alpha = 0.45f), shape)
            .background(cs.surface)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Activity chart is hidden. Tap Manage to turn it back on.",
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ManageChartBottomSheet(
    showChart: Boolean,
    onShowChartChange: (Boolean) -> Unit,
    chartType: String,
    onChartTypeChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cs.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "Manage chart",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface,
                    modifier = Modifier.align(Alignment.Center),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = cs.onSurface,
                    )
                }
            }

            Text(
                text = "Visibility",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Show Chart",
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onSurface,
                )
                Switch(
                    checked = showChart,
                    onCheckedChange = onShowChartChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = cs.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = cs.outline,
                    ),
                )
            }

            Text(
                text = "Chart Type",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp),
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = cs.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ChartTypeOptionRow(
                        selected = chartType == ActivityChartType.FOUR_WEEK,
                        icon = Icons.Default.GridOn,
                        title = "4 Week Activity",
                        description = "View your last 28 days of focus time in a heatmap calendar.",
                        onSelect = { onChartTypeChange(ActivityChartType.FOUR_WEEK) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = cs.outlineVariant,
                    )
                    ChartTypeOptionRow(
                        selected = chartType == ActivityChartType.WEEKLY,
                        icon = Icons.Default.BarChart,
                        title = "Weekly View",
                        description = "See your week-by-week focus patterns with bar charts.",
                        onSelect = { onChartTypeChange(ActivityChartType.WEEKLY) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = cs.outlineVariant,
                    )
                    ChartTypeOptionRow(
                        selected = chartType == ActivityChartType.MONTHLY,
                        icon = Icons.Default.CalendarMonth,
                        title = "Monthly View",
                        description = "Track your monthly progress with a calendar grid.",
                        onSelect = { onChartTypeChange(ActivityChartType.MONTHLY) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChartTypeOptionRow(
    selected: Boolean,
    icon: ImageVector,
    title: String,
    description: String,
    onSelect: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = cs.primary,
                unselectedColor = cs.onSurfaceVariant,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = cs.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 10.dp)
                .size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = cs.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
            )
        }
    }
}

private fun profileRestrictionCount(profile: Profile): Int =
    profile.blockedPackages.size + profile.domains.size

private fun formatProfileUpdatedAgo(updatedAtMs: Long, nowMs: Long): String {
    val s = ((nowMs - updatedAtMs) / 1000).coerceAtLeast(0)
    return when {
        s < 60 -> "Updated ${s} sec ago"
        s < 3600 -> "Updated ${s / 60} min ago"
        s < 86400 -> "Updated ${s / 3600} hr ago"
        else -> "Updated ${s / 86400} days ago"
    }
}

@Composable
private fun ProfilesManageFullScreen(
    profiles: List<Profile>,
    onDismiss: () -> Unit,
    onCreateProfile: () -> Unit,
    onEditProfile: (String) -> Unit,
) {
    BackHandler(onBack = onDismiss)
    val cs = MaterialTheme.colorScheme
    var headerMenuExpanded by remember { mutableStateOf(false) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            nowMs = System.currentTimeMillis()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cs.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(cs.surfaceVariant),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = cs.onSurface,
                    )
                }
                Box {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = cs.surface,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, cs.outline.copy(alpha = 0.22f)),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { headerMenuExpanded = true },
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "More options",
                                    tint = cs.onSurface,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(22.dp)
                                    .background(cs.outline.copy(alpha = 0.28f)),
                            )
                            IconButton(
                                onClick = onCreateProfile,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add profile",
                                    tint = cs.onSurface,
                                )
                            }
                        }
                    }
                    DropdownMenu(
                        expanded = headerMenuExpanded,
                        onDismissRequest = { headerMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by name") },
                            onClick = { headerMenuExpanded = false },
                        )
                    }
                }
            }

            Text(
                text = "Profiles",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            )

            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Text(
                        text = "No profiles yet. Tap + to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        ProfilesManageListCard(
                            profile = profile,
                            nowMs = nowMs,
                            onClick = { onEditProfile(profile.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilesManageListCard(
    profile: Profile,
    nowMs: Long,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val itemsCount = profileRestrictionCount(profile)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, cs.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name.ifBlank { "Unnamed Profile" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = formatProfileUpdatedAgo(profile.updatedAt, nowMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(cs.surfaceVariant.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = null,
                        tint = cs.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = "$itemsCount items",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProfileRegionHeader(onManage: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = cs.onSurfaceVariant,
        )
        ManagePill(
            icon = Icons.Default.Person,
            onClick = onManage,
        )
    }
}

@Composable
private fun ManagePill(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, cs.outline.copy(alpha = 0.55f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = cs.onSurface,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Manage",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ActivityHeatmapCard() {
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val cardShape = RoundedCornerShape(22.dp)
    val cellShape = RoundedCornerShape(8.dp)
    val cellHeight = 38.dp
    val rowGap = 8.dp
    val legendColors = listOf(
        Color(0xFF3A3A3C),
        Color(0xFF8B7355),
        Color(0xFFC4A77D),
        Color(0xFFE8D4B8),
    )
    val legendLabels = listOf("<1h", "1–3h", "3–5h", ">5h")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 248.dp)
            .clip(cardShape)
            .border(1.dp, cs.outline.copy(alpha = 0.45f), cardShape)
            .background(cs.surface)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            legendColors.forEachIndexed { i, c ->
                if (i > 0) Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(c),
                    )
                    Text(
                        text = legendLabels[i],
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant,
                        fontSize = 9.sp,
                    )
                }
            }
        }
        val startDay = 21
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(7) { col ->
                Text(
                    text = "${startDay + col}",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(rowGap),
        ) {
            repeat(4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    repeat(7) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .clip(cellShape)
                                .background(
                                    cs.surfaceVariant.copy(
                                        alpha = if (isDark) 0.55f else 0.85f,
                                    ),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileList(
    profiles: List<Profile>,
    activeSession: Session?,
    sessionCounts: Map<String, Int>,
    onEditProfile: (String) -> Unit,
    onInsightsProfile: (Profile) -> Unit,
    onStartSession: (String) -> Unit,
    onStopSession: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        profiles.forEach { profile ->
            val isActive = activeSession?.profileId == profile.id
            val anotherSessionActive = activeSession != null && !isActive
            ProfileCard(
                profile = profile,
                sessionCount = sessionCounts[profile.id] ?: 0,
                isActive = isActive,
                anotherSessionActive = anotherSessionActive,
                activeSession = if (isActive) activeSession else null,
                onEdit = { onEditProfile(profile.id) },
                onInsights = { onInsightsProfile(profile) },
                onStart = { onStartSession(profile.id) },
                onStop = onStopSession,
            )
        }
    }
}

@Composable
private fun ProfileCardOverflowMenu(
    profileId: String,
    onEdit: () -> Unit,
    onInsights: () -> Unit,
    onStart: () -> Unit,
    isActive: Boolean,
    anotherSessionActive: Boolean,
) {
    var expanded by remember(profileId) { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val menuBg = if (isDark) Color(0xEC2C2C2E) else Color(0xF5FAFAFA)
    val itemColor = if (isDark) Color.White else cs.onSurface
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.12f) else cs.outline.copy(alpha = 0.35f)

    Box {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, cs.outline.copy(alpha = 0.4f), CircleShape)
                .background(cs.surface.copy(alpha = 0.6f))
                .clickable { expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Profile options",
                tint = cs.onSurface,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 220.dp),
            shape = RoundedCornerShape(18.dp),
            containerColor = menuBg,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, cs.outline.copy(alpha = if (isDark) 0.28f else 0.22f)),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Edit",
                        color = itemColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp),
                    )
                },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = dividerColor,
                thickness = 0.5.dp,
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Insights",
                        color = itemColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp),
                    )
                },
                onClick = {
                    expanded = false
                    onInsights()
                },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = dividerColor,
                thickness = 0.5.dp,
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Start",
                        color = itemColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp),
                    )
                },
                onClick = {
                    expanded = false
                    if (!isActive && !anotherSessionActive) onStart()
                },
                enabled = !isActive && !anotherSessionActive,
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    sessionCount: Int,
    isActive: Boolean,
    anotherSessionActive: Boolean,
    activeSession: Session?,
    onEdit: () -> Unit,
    onInsights: () -> Unit = {},
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val strategy = strategyInfoById(profile.strategyId)
    val accent = LocalAccentColor.current.value
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val cardShape = RoundedCornerShape(24.dp)
    val strategyIcon = when (strategy?.icon) {
        "nfc" -> Icons.Default.Nfc
        "timer" -> Icons.Default.Timer
        "pause" -> Icons.Default.Pause
        else -> Icons.Default.Nfc
    }
    val strategyTitle = strategy?.name ?: "Tyme Boxed"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, cs.outline.copy(alpha = 0.45f), cardShape),
    ) {
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.surface),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = if (isDark) 0.42f else 0.28f),
                                accent.copy(alpha = if (isDark) 0.12f else 0.08f),
                                Color.Transparent,
                            ),
                            center = Offset(w * 0.92f, h * 0.35f),
                            radius = w * 0.85f,
                        ),
                    ),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = profile.name.ifBlank { "Unnamed Profile" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEdit() },
                )
                ProfileCardOverflowMenu(
                    profileId = profile.id,
                    onEdit = onEdit,
                    onInsights = onInsights,
                    onStart = onStart,
                    isActive = isActive,
                    anotherSessionActive = anotherSessionActive,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(cs.onSurface),
                )
                Text(
                    text = if (profile.enableStrictMode) "Strict" else "Standard",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = strategyIcon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = strategyTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                VerticalDivider(
                    modifier = Modifier.height(28.dp),
                    color = cs.outlineVariant,
                )
                Text(
                    text = "No Schedule Set",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ProfileStatColumn(
                    label = "Apps & Categories",
                    value = profile.blockedPackages.size,
                )
                ProfileStatColumn(
                    label = "Domains",
                    value = profile.domains.size,
                )
                ProfileStatColumn(
                    label = "Total Sessions",
                    value = sessionCount,
                )
            }

            if (isActive && activeSession != null) {
                ActiveSessionRow(
                    startTime = activeSession.startTime,
                    onStop = onStop,
                )
            } else {
                HoldToStartBar(
                    enabled = !anotherSessionActive,
                    onHoldComplete = onStart,
                )
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(
    label: String,
    value: Int,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.widthIn(min = 92.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = cs.onSurface,
        )
    }
}

@Composable
private fun HoldToStartBar(
    enabled: Boolean,
    onHoldComplete: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val bg = if (enabled) {
        cs.surfaceVariant.copy(alpha = 0.85f)
    } else {
        cs.surfaceVariant.copy(alpha = 0.45f)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(bg)
            .then(
                if (enabled) {
                    Modifier.combinedClickable(
                        onClick = { },
                        onLongClick = { onHoldComplete() },
                        onLongClickLabel = "Start blocking session",
                    )
                } else {
                    Modifier
                },
            )
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = if (enabled) cs.onSurface else cs.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Hold to Start",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) cs.onSurface else cs.onSurface.copy(alpha = 0.38f),
        )
    }
}

/** Active session row: elapsed timer on the left, Stop button on the right. */
@Composable
private fun ActiveSessionRow(
    startTime: Long,
    onStop: () -> Unit,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }
    val elapsed = (now - startTime).coerceAtLeast(0) / 1000
    val hours = elapsed / 3600
    val minutes = (elapsed % 3600) / 60
    val seconds = elapsed % 60
    val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Elapsed time chip
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = timerText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Stop button
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text("Stop")
        }
    }
}

