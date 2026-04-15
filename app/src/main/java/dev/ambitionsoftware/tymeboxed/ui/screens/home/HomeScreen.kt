package dev.ambitionsoftware.tymeboxed.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.os.Build
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository
import dev.ambitionsoftware.tymeboxed.domain.model.Profile
import dev.ambitionsoftware.tymeboxed.domain.model.Session
import dev.ambitionsoftware.tymeboxed.domain.model.strategyInfoById
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel
import dev.ambitionsoftware.tymeboxed.service.ActiveBlockingState
import dev.ambitionsoftware.tymeboxed.service.SessionBlockerService
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCard
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
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

    init {
        // Rehydrate blocking state if the app was killed while a session was active.
        viewModelScope.launch {
            val session = sessionRepository.findActive() ?: return@launch
            if (ActiveBlockingState.current.isBlocking) return@launch
            val profile = profileRepository.findById(session.profileId) ?: return@launch
            ActiveBlockingState.activate(
                profileId = session.profileId,
                blockedPackages = profile.blockedPackages.toSet(),
                isAllowMode = profile.isAllowMode,
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
    val allGranted by permissionsVm.allRequiredGranted.collectAsState()

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

    val homeBg = Color(0xFF000000)
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
                    color = Color.White,
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
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
                    ProfileList(
                        profiles = profiles,
                        activeSession = activeSession,
                        onEditProfile = onEditProfile,
                        onStartSession = { vm.startSession(it) },
                        onStopSession = { vm.stopSession() },
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // room for FAB
        }
    }
}

/**
 * Empty-state hero card — visual twin of iOS `Welcome.swift` (hourglass chip,
 * theme blob, “Welcome to Tyme Boxed” copy). Tapping opens profile creation.
 */
@Composable
private fun WelcomeEmptyCard(onTap: () -> Unit) {
    val cardShape = RoundedCornerShape(28.dp)
    val hourglassGold = Color(0xFFC4A77D)
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
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(hourglassGold),
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
                color = Color.White,
                fontSize = 26.sp,
            )
            Text(
                text = "Tyme Boxed",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 28.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tap here to get started on your first profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFBBBBBB),
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun ProfileList(
    profiles: List<Profile>,
    activeSession: Session?,
    onEditProfile: (String) -> Unit,
    onStartSession: (String) -> Unit,
    onStopSession: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        profiles.forEach { profile ->
            val isActive = activeSession?.profileId == profile.id
            val anotherSessionActive = activeSession != null && !isActive
            ProfileCard(
                profile = profile,
                isActive = isActive,
                anotherSessionActive = anotherSessionActive,
                activeSession = if (isActive) activeSession else null,
                onClick = { onEditProfile(profile.id) },
                onStart = { onStartSession(profile.id) },
                onStop = onStopSession,
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    isActive: Boolean,
    anotherSessionActive: Boolean,
    activeSession: Session?,
    onClick: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val strategy = strategyInfoById(profile.strategyId)
    val strategyColor = strategy?.color ?: MaterialTheme.colorScheme.primary

    SettingsCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Profile name
            Text(
                text = profile.name.ifBlank { "Unnamed Profile" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Strategy badge row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Strategy chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(strategyColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val icon = when (strategy?.icon) {
                        "nfc" -> Icons.Default.Nfc
                        "timer" -> Icons.Default.Timer
                        "pause" -> Icons.Default.Pause
                        else -> Icons.Default.Nfc
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = strategyColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = strategy?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = strategyColor,
                    )
                }

                // Feature indicator icons
                if (profile.enableLiveActivity) {
                    FeatureIcon(Icons.Default.Notifications, "Live Activity")
                }
                if (profile.reminderTimeSeconds != null) {
                    FeatureIcon(Icons.Default.Timer, "Reminder")
                }
                if (profile.enableBreaks) {
                    FeatureIcon(Icons.Default.Pause, "Breaks")
                }
                if (profile.enableStrictMode) {
                    FeatureIcon(Icons.Default.Security, "Strict")
                }
            }

            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatItem(
                    icon = Icons.Default.Apps,
                    count = profile.blockedPackages.size,
                    label = "apps",
                )
                if (profile.domains.isNotEmpty()) {
                    StatItem(
                        icon = Icons.Default.Language,
                        count = profile.domains.size,
                        label = "domains",
                    )
                }
            }

            // Start / Stop button — matches iOS ProfileTimerButton
            if (isActive && activeSession != null) {
                ActiveSessionRow(
                    startTime = activeSession.startTime,
                    onStop = onStop,
                )
            } else {
                Button(
                    onClick = onStart,
                    enabled = !anotherSessionActive,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Start Session")
                }
            }
        }
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

@Composable
private fun FeatureIcon(icon: ImageVector, description: String) {
    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp),
    )
}

@Composable
private fun StatItem(icon: ImageVector, count: Int, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
