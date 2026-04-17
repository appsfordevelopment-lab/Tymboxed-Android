package dev.ambitionsoftware.tymeboxed.ui.screens.settings

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
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
import androidx.core.net.toUri
import dev.ambitionsoftware.tymeboxed.BuildConfig
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
    val states by permissionsVm.states.collectAsState()
    val allRequiredGranted by permissionsVm.allRequiredGranted.collectAsState()

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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsCloseButton(onClick = onBack)
            }

            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
            )

            ThemeCard(
                currentAccent = accent,
                onSelect = settingsVm::selectAccent,
            )

            AboutCard(
                accessAuthorized = allRequiredGranted,
                onBuyDevice = {
                    runCatching {
                        ctx.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://www.tymeboxed.app/".toUri(),
                            ),
                        )
                    }
                },
            )

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
private fun SettingsCloseButton(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(cs.surfaceVariant),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close settings",
            tint = cs.onSurface,
        )
    }
}

@Composable
private fun ThemeCard(
    currentAccent: AccentColor,
    onSelect: (AccentColor) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    SettingsCard(title = "Theme", elevation = 0.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(cs.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = cs.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurface,
                    )
                    Text(
                        text = "Customize the look of your app",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                    )
                }
            }
            SettingsCardDivider()
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { menuOpen = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Theme Color",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurface,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = currentAccent.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant,
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant,
                        )
                    }
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                ) {
                    AccentColors.all.forEach { accentOption ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(accentOption.value),
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(accentOption.name)
                                }
                            },
                            onClick = {
                                onSelect(accentOption)
                                menuOpen = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutCard(
    accessAuthorized: Boolean,
    onBuyDevice: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val versionLabel = "v${BuildConfig.VERSION_NAME}"
    SettingsCard(title = "About", elevation = 0.dp) {
        LabelRow(label = "Version", value = versionLabel)
        SettingsCardDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Screen Time Access",
                style = MaterialTheme.typography.bodyLarge,
                color = cs.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (accessAuthorized) Color(0xFF34C759) else Color(0xFFFF9500),
                        ),
                )
                Text(
                    text = if (accessAuthorized) "Authorized" else "Action needed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                )
            }
        }
        SettingsCardDivider()
        LabelRow(label = "Made in", value = "Hyderabad India 🇮🇳")
        SettingsCardDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBuyDevice)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Buy a Tyme Boxed device",
                style = MaterialTheme.typography.bodyLarge,
                color = cs.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
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
    SettingsCard(title = "Permissions", elevation = 0.dp) {
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
    SettingsCard(title = "Troubleshooting", elevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onResetBlockingState() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reset Blocking State",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DangerCard(onDeleteAllData: () -> Unit) {
    SettingsCard(title = null, elevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDeleteAllData() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Delete Account",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun openPermissionIntent(context: Context, perm: TymePermission) {
    runCatching {
        context.startActivity(PermissionIntents.intentFor(context, perm))
    }
}
