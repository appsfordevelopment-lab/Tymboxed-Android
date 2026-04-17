package dev.ambitionsoftware.tymeboxed.ui.screens.profile

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.ambitionsoftware.tymeboxed.domain.model.StrategyInfo
import dev.ambitionsoftware.tymeboxed.domain.model.availableStrategies
import dev.ambitionsoftware.tymeboxed.ui.components.ActionButton
import dev.ambitionsoftware.tymeboxed.ui.components.CustomToggle
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCard
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCardDivider
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCardRow
import dev.ambitionsoftware.tymeboxed.ui.screens.insights.ProfileInsightsScreen

@Composable
fun ProfileEditScreen(
    profileId: String,
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
) {
    val vm: ProfileEditViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onBack()
    }
    LaunchedEffect(state.deletedSuccessfully) {
        if (state.deletedSuccessfully) onBack()
    }

    val pendingNavigationProfileId by vm.pendingNavigationProfileId.collectAsState()
    LaunchedEffect(pendingNavigationProfileId) {
        val id = pendingNavigationProfileId ?: return@LaunchedEffect
        onNavigateToProfile(id)
        vm.consumePendingNavigation()
    }

    var showInsights by remember { mutableStateOf(false) }
    var showSessions by remember { mutableStateOf(false) }
    var showOverflowDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileIosIconButton(
                        onClick = onBack,
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!state.isNew && state.profileReady) {
                        ProfileEditTopActionPill(
                            onInsights = { showInsights = true },
                            onDuplicate = { vm.duplicateProfile() },
                            onViewSessions = { showSessions = true },
                            onDeleteFromMenu = { showOverflowDeleteConfirm = true },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    ProfileIosIconButton(
                        onClick = { vm.save() },
                        enabled = !state.isSaving,
                        icon = Icons.Default.Check,
                        contentDescription = if (state.isNew) "Create profile" else "Save profile",
                    )
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Error banner
                if (state.errorMessage != null) {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = state.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                Text(
                    text = if (state.isNew) "Create New Profile" else "Profile Details",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                )

                // 1. Name
                NameSection(
                    name = state.name,
                    onNameChange = vm::onNameChange,
                )

                // 2. Blocked apps
                AppPickerSection(
                    isAllowMode = state.isAllowMode,
                    onAllowModeChange = vm::onAllowModeChange,
                    selectedPackages = state.blockedPackages,
                    installedApps = state.installedApps,
                    onToggleApp = vm::onToggleApp,
                )

                // 3. Blocked domains
                DomainSection(
                    isAllowModeDomains = state.isAllowModeDomains,
                    onAllowModeDomainsChange = vm::onAllowModeDomainsChange,
                    domains = state.domains,
                    onAddDomain = vm::onAddDomain,
                    onRemoveDomain = vm::onRemoveDomain,
                )

                // 4. Schedule (UI parity with iOS; scheduling logic TBD)
                ScheduleSection()

                // 5. Blocking strategy
                StrategySection(
                    selectedId = state.strategyId,
                    onSelect = vm::onStrategyChange,
                    timerMinutes = state.timerMinutes,
                    onTimerChange = vm::onTimerMinutesChange,
                )

                // 6. Breaks
                BreaksSection(
                    enableBreaks = state.enableBreaks,
                    onBreaksChange = vm::onBreaksChange,
                    breakTimeInMinutes = state.breakTimeInMinutes,
                    onBreakTimeChange = vm::onBreakTimeChange,
                )

                // 7. Safeguards
                SafeguardsSection(
                    enableStrictMode = state.enableStrictMode,
                    onStrictModeChange = vm::onStrictModeChange,
                )

                // 8. Notifications
                NotificationsSection(
                    enableLiveActivity = state.enableLiveActivity,
                    onLiveActivityChange = vm::onLiveActivityChange,
                    enableReminder = state.enableReminder,
                    onReminderChange = vm::onReminderChange,
                    reminderTimeMinutes = state.reminderTimeMinutes,
                    onReminderTimeChange = vm::onReminderTimeChange,
                    customReminderMessage = state.customReminderMessage,
                    onReminderMessageChange = vm::onReminderMessageChange,
                    onOpenNotificationSettings = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            putExtra("app_package", context.packageName)
                            putExtra("app_uid", context.applicationInfo.uid)
                        }
                        runCatching { context.startActivity(intent) }
                    },
                )

                // Delete (edit mode only)
                if (!state.isNew) {
                    DeleteSection(
                        isDeleting = state.isDeleting,
                        onDelete = vm::delete,
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showInsights) {
            vm.profileForInsights()?.let { p ->
                ProfileInsightsScreen(
                    profile = p,
                    onDismiss = { showInsights = false },
                )
            }
        }

        if (showSessions && !state.isNew) {
            ProfileSessionsScreen(
                profileId = profileId,
                profileName = state.name,
                onDismiss = { showSessions = false },
            )
        }

        if (showOverflowDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showOverflowDeleteConfirm = false },
                title = { Text("Delete Profile") },
                text = {
                    Text(
                        "Are you sure you want to delete this profile? " +
                            "It will be removed from this device. This cannot be undone.",
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showOverflowDeleteConfirm = false
                        vm.delete()
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOverflowDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}

@Composable
private fun ProfileEditTopActionPill(
    onInsights: () -> Unit,
    onDuplicate: () -> Unit,
    onViewSessions: () -> Unit,
    onDeleteFromMenu: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val menuBg = if (isDark) Color(0xEC2C2C2E) else Color(0xF5FAFAFA)
    val itemColor = if (isDark) Color.White else cs.onSurface
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.12f) else cs.outline.copy(alpha = 0.35f)
    val errorColor = MaterialTheme.colorScheme.error
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = cs.surface,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, cs.outline.copy(alpha = 0.22f)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { menuExpanded = true },
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
                    onClick = onInsights,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Insights",
                        tint = cs.onSurface,
                    )
                }
            }
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.widthIn(min = 260.dp),
            shape = RoundedCornerShape(18.dp),
            containerColor = menuBg,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, cs.outline.copy(alpha = if (isDark) 0.28f else 0.22f)),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Duplicate Profile",
                        color = itemColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp),
                    )
                },
                onClick = {
                    menuExpanded = false
                    onDuplicate()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "View Sessions",
                        color = itemColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp),
                    )
                },
                onClick = {
                    menuExpanded = false
                    onViewSessions()
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
                        "Delete Profile",
                        color = errorColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = errorColor,
                        modifier = Modifier.size(22.dp),
                    )
                },
                onClick = {
                    menuExpanded = false
                    onDeleteFromMenu()
                },
            )
        }
    }
}

@Composable
private fun ProfileIosIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    contentDescription: String,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
        )
    }
}

// ─── Name ────────────────────────────────────────────────────────────────────

@Composable
private fun NameSection(
    name: String,
    onNameChange: (String) -> Unit,
) {
    SettingsCard(title = "Name", elevation = 0.dp) {
        SettingsCardRow {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = {
                    Text(
                        "Profile Name",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}

// ─── Strategy ────────────────────────────────────────────────────────────────

private fun strategyIcon(iconHint: String): ImageVector = when (iconHint) {
    "nfc" -> Icons.Default.Nfc
    "timer" -> Icons.Default.Timer
    "pause" -> Icons.Default.Pause
    "touch_app" -> Icons.Default.TouchApp
    else -> Icons.Default.Nfc
}

@Composable
private fun StrategySection(
    selectedId: String,
    onSelect: (String) -> Unit,
    timerMinutes: Int,
    onTimerChange: (Int) -> Unit,
) {
    val visible = availableStrategies.filter { !it.hidden }

    SettingsCard(title = "Blocking Strategy", elevation = 0.dp) {
        visible.forEachIndexed { index, strategy ->
            StrategyRow(
                strategy = strategy,
                isSelected = selectedId == strategy.id,
                onClick = { onSelect(strategy.id) },
            )
            if (index < visible.lastIndex) SettingsCardDivider()
        }

        // Timer duration (for strategies that have timers)
        val currentStrategy = availableStrategies.find { it.id == selectedId }
        if (currentStrategy?.hasTimer == true) {
            SettingsCardDivider()
            SettingsCardRow {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = timerMinutes.toString(),
                            onValueChange = { text ->
                                text.toIntOrNull()?.let { onTimerChange(it) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(72.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyRow(
    strategy: StrategyInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val pillBg = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(pillBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = strategyIcon(strategy.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = strategy.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = strategy.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
            )
            if (strategy.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    strategy.tags.forEach { tag ->
                        Text(
                            text = tag.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pillBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 0.dp else 2.dp,
                    color = if (isSelected) Color.Transparent else outline,
                    shape = CircleShape,
                )
                .background(if (isSelected) accent else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ─── App picker ──────────────────────────────────────────────────────────────

@Composable
private fun AppPickerSection(
    isAllowMode: Boolean,
    onAllowModeChange: (Boolean) -> Unit,
    selectedPackages: Set<String>,
    installedApps: List<InstalledApp>,
    onToggleApp: (String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val selectedCount = selectedPackages.size

    SettingsCard(title = "Blocked Apps", elevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Select Apps to Restrict",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingsCardDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            if (selectedCount == 0) {
                Text(
                    text = "No apps selected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "$selectedCount app${if (selectedCount != 1) "s" else ""} selected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val preview = installedApps
                    .filter { selectedPackages.contains(it.packageName) }
                    .take(3)
                    .joinToString(", ") { it.label }
                val suffix = if (selectedCount > 3) " +${selectedCount - 3} more" else ""
                Text(
                    text = preview + suffix,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        SettingsCardDivider()

        SettingsCardRow {
            CustomToggle(
                title = "Apps Allow Mode",
                description = "Pick apps to allow and block everything else. This will erase any other selection you've made.",
                checked = isAllowMode,
                onCheckedChange = onAllowModeChange,
            )
        }

        if (expanded) {
            SettingsCardDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            val filtered = if (searchQuery.isBlank()) installedApps
            else installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Text(
                    text = if (installedApps.isEmpty()) "Loading apps…"
                    else "No apps match your search",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                filtered.forEach { app ->
                    AppRow(
                        app = app,
                        isSelected = selectedPackages.contains(app.packageName),
                        onToggle = { onToggleApp(app.packageName) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // App icon
        val iconDrawable = remember(app.packageName) {
            try {
                context.packageManager.getApplicationIcon(app.packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
        if (iconDrawable != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(iconDrawable)
                    .crossfade(true)
                    .build(),
                contentDescription = app.label,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

// ─── Domains ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DomainSection(
    isAllowModeDomains: Boolean,
    onAllowModeDomainsChange: (Boolean) -> Unit,
    domains: List<String>,
    onAddDomain: (String) -> Unit,
    onRemoveDomain: (String) -> Unit,
) {
    var newDomain by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    SettingsCard(title = "Blocked Domains", elevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Select Domains to Restrict",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingsCardDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            if (domains.isEmpty()) {
                Text(
                    text = "No domains selected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "${domains.size} domain${if (domains.size != 1) "s" else ""} selected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = domains.take(4).joinToString(", ") +
                        if (domains.size > 4) " +${domains.size - 4} more" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        SettingsCardDivider()

        SettingsCardRow {
            CustomToggle(
                title = "Domain Allow Mode",
                description = "Pick domains to allow and block everything else. This will erase any other selection you've made.",
                checked = isAllowModeDomains,
                onCheckedChange = onAllowModeDomainsChange,
            )
        }

        if (expanded) {
            SettingsCardDivider()
            if (domains.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    domains.forEach { domain ->
                        DomainChip(
                            domain = domain,
                            onRemove = { onRemoveDomain(domain) },
                        )
                    }
                }
                SettingsCardDivider()
            }
            SettingsCardRow {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newDomain,
                        onValueChange = { newDomain = it },
                        placeholder = { Text("example.com") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            onAddDomain(newDomain)
                            newDomain = ""
                        },
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add domain",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DomainChip(
    domain: String,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = domain,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ─── Schedule (UI parity with iOS; scheduling logic not yet on Android) ────

@Composable
private fun ScheduleSection() {
    SettingsCard(title = "Schedule", elevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Set schedule",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SettingsCardDivider()
        Text(
            text = "No Schedule Set",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

// ─── Breaks ──────────────────────────────────────────────────────────────────

private val breakOptions = listOf(5, 10, 15, 30)

@Composable
private fun BreaksSection(
    enableBreaks: Boolean,
    onBreaksChange: (Boolean) -> Unit,
    breakTimeInMinutes: Int,
    onBreakTimeChange: (Int) -> Unit,
) {
    SettingsCard(title = "Breaks", elevation = 0.dp) {
        SettingsCardRow {
            CustomToggle(
                title = "Allow Timed Breaks",
                description = "Take a single break during your session. The break will automatically end after the selected duration.",
                checked = enableBreaks,
                onCheckedChange = onBreaksChange,
            )
        }
        if (enableBreaks) {
            SettingsCardDivider()
            SettingsCardRow {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Break Duration",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        breakOptions.forEach { minutes ->
                            val selected = breakTimeInMinutes == minutes
                            Text(
                                text = "${minutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.12f,
                                        )
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.35f,
                                        ),
                                    )
                                    .clickable { onBreakTimeChange(minutes) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Safeguards ──────────────────────────────────────────────────────────────

@Composable
private fun SafeguardsSection(
    enableStrictMode: Boolean,
    onStrictModeChange: (Boolean) -> Unit,
) {
    SettingsCard(title = "Safeguards", elevation = 0.dp) {
        SettingsCardRow {
            CustomToggle(
                title = "Strict",
                description = "Block deleting apps from your phone, stops you from deleting Tyme Boxed to access apps.",
                checked = enableStrictMode,
                onCheckedChange = onStrictModeChange,
            )
        }
    }
}

// ─── Notifications ───────────────────────────────────────────────────────────

@Composable
private fun NotificationsSection(
    enableLiveActivity: Boolean,
    onLiveActivityChange: (Boolean) -> Unit,
    enableReminder: Boolean,
    onReminderChange: (Boolean) -> Unit,
    reminderTimeMinutes: Int,
    onReminderTimeChange: (Int) -> Unit,
    customReminderMessage: String,
    onReminderMessageChange: (String) -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    SettingsCard(title = "Notifications", elevation = 0.dp) {
        SettingsCardRow {
            CustomToggle(
                title = "Live Activity",
                description = "Shows a live activity on your lock screen with some inspirational quote.",
                checked = enableLiveActivity,
                onCheckedChange = onLiveActivityChange,
            )
        }

        SettingsCardDivider()

        SettingsCardRow {
            CustomToggle(
                title = "Reminder",
                description = "Sends a reminder to start this profile when its ended.",
                checked = enableReminder,
                onCheckedChange = onReminderChange,
            )
        }

        if (enableReminder) {
            SettingsCardDivider()

            // Reminder time
            SettingsCardRow {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Reminder time",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = reminderTimeMinutes.toString(),
                            onValueChange = { text ->
                                text.toIntOrNull()?.let { onReminderTimeChange(it) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(72.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            SettingsCardDivider()

            // Custom message
            SettingsCardRow {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Reminder message",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    OutlinedTextField(
                        value = customReminderMessage,
                        onValueChange = onReminderMessageChange,
                        placeholder = { Text("Time to focus! Start your session.") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        supportingText = {
                            Text(
                                text = "${customReminderMessage.length}/178",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }
        }

        SettingsCardDivider()

        Text(
            text = "Go to settings to disable globally.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenNotificationSettings)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

// ─── Delete ──────────────────────────────────────────────────────────────────

@Composable
private fun DeleteSection(
    isDeleting: Boolean,
    onDelete: () -> Unit,
) {
    var showConfirmation by remember { mutableStateOf(false) }

    ActionButton(
        title = "Delete Profile",
        onClick = { showConfirmation = true },
        icon = Icons.Default.Delete,
        backgroundColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        isLoading = isDeleting,
    )

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Delete Profile") },
            text = {
                Text(
                    "Are you sure you want to delete this profile? " +
                        "It will be removed from this device. This cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    onDelete()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
