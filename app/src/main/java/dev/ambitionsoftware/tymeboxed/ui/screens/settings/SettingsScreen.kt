package dev.ambitionsoftware.tymeboxed.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository
import dev.ambitionsoftware.tymeboxed.service.ActiveBlockingState
import dev.ambitionsoftware.tymeboxed.permissions.PermissionIntents
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel
import dev.ambitionsoftware.tymeboxed.permissions.TymePermission
import dev.ambitionsoftware.tymeboxed.ui.components.PermissionRow
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCard
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCardDivider
import dev.ambitionsoftware.tymeboxed.ui.theme.AccentColor
import dev.ambitionsoftware.tymeboxed.ui.theme.AccentColors
import dev.ambitionsoftware.tymeboxed.ui.theme.ThemeController
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeController: ThemeController,
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val accent: StateFlow<AccentColor> = themeController.accent.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AccentColors.default,
    )

    val isBlocking: StateFlow<Boolean> = sessionRepository.activeSession
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun selectAccent(accent: AccentColor) {
        viewModelScope.launch { themeController.select(accent) }
    }

    fun resetBlockingState() {
        viewModelScope.launch {
            ActiveBlockingState.deactivate()
            sessionRepository.resetActive()
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            ActiveBlockingState.deactivate()
            sessionRepository.resetActive()
            profileRepository.deleteAll()
        }
    }
}

/**
 * Settings screen — mirrors iOS [`SettingsView`] card layout:
 *   1. Theme (Appearance) card — accent swatch + dropdown of all 15 colours.
 *   2. About card — version + blocking status + "Made in Hyderabad India".
 *   3. Permissions card — one [PermissionRow] per [TymePermission]. Android-only.
 *   4. Troubleshooting card — reset blocking state.
 *   5. Danger card — delete all data.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenFullPermissions: () -> Unit,
) {
    val settingsVm: SettingsViewModel = hiltViewModel()
    val permissionsVm: PermissionsViewModel = hiltViewModel()
    val accent by settingsVm.accent.collectAsState()
    val isBlocking by settingsVm.isBlocking.collectAsState()
    val states by permissionsVm.states.collectAsState()

    // Refresh permission states on resume.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionsVm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val ctx = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
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
            ThemeCard(
                currentAccent = accent,
                onSelect = settingsVm::selectAccent,
            )

            AboutCard(isBlocking = isBlocking)

            PermissionsCard(
                states = states,
                isNfcAvailable = permissionsVm.isNfcAvailable,
                onGrantClick = { perm -> openPermissionIntent(ctx, perm) },
                onOpenFull = onOpenFullPermissions,
            )

            TroubleshootingCard(
                onResetBlockingState = settingsVm::resetBlockingState,
            )

            DangerCard(
                onDeleteAllData = settingsVm::deleteAllData,
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ThemeCard(
    currentAccent: AccentColor,
    onSelect: (AccentColor) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    SettingsCard(title = "Appearance") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { menuOpen = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(currentAccent.value),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Theme Color",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = currentAccent.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                AccentColors.all.forEach { accent ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(accent.value),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(accent.name)
                            }
                        },
                        onClick = {
                            onSelect(accent)
                            menuOpen = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutCard(isBlocking: Boolean) {
    SettingsCard(title = "About") {
        LabelRow(label = "Version", value = "1.0")
        SettingsCardDivider()
        LabelRow(
            label = "Blocking Status",
            value = if (isBlocking) "Blocked" else "Not Blocking",
            valueColor = if (isBlocking) MaterialTheme.colorScheme.primary else null,
        )
        SettingsCardDivider()
        LabelRow(label = "Made in", value = "Hyderabad, India")
    }
}

@Composable
private fun LabelRow(
    label: String,
    value: String,
    valueColor: Color? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PermissionsCard(
    states: Map<TymePermission, Boolean>,
    isNfcAvailable: Boolean,
    onGrantClick: (TymePermission) -> Unit,
    onOpenFull: () -> Unit,
) {
    SettingsCard(title = "Permissions") {
        val perms = TymePermission.entries
        perms.forEachIndexed { idx, perm ->
            val nfcUnavailable = perm == TymePermission.NFC && !isNfcAvailable
            PermissionRow(
                permission = perm,
                granted = states[perm] == true,
                onGrantClick = { onGrantClick(perm) },
                unavailable = nfcUnavailable,
            )
            if (idx < perms.lastIndex) SettingsCardDivider()
        }
        SettingsCardDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenFull() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Open full permissions screen",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TroubleshootingCard(onResetBlockingState: () -> Unit) {
    SettingsCard(title = "Troubleshooting") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onResetBlockingState() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reset Blocking State",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Ends any active session and clears lingering blocking state.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DangerCard(onDeleteAllData: () -> Unit) {
    SettingsCard(title = "Danger Zone") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDeleteAllData() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Delete All Data",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "Removes every profile, session, and tag from this device. Cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun openPermissionIntent(context: Context, perm: TymePermission) {
    runCatching {
        context.startActivity(PermissionIntents.intentFor(context, perm))
    }
}
