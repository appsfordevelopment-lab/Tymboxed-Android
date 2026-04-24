package dev.ambitionsoftware.tymeboxed.ui.screens.permissions

import android.content.Context
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.ambitionsoftware.tymeboxed.permissions.PermissionIntents
import dev.ambitionsoftware.tymeboxed.permissions.PermissionsViewModel
import dev.ambitionsoftware.tymeboxed.permissions.TymePermission
import dev.ambitionsoftware.tymeboxed.R
import dev.ambitionsoftware.tymeboxed.ui.components.PermissionRow
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCard
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsCardDivider
import dev.ambitionsoftware.tymeboxed.ui.screens.settings.SettingsViewModel

/**
 * Standalone permissions screen — Android equivalent of iOS
 * [`PermissionsScreen`](Components/Intro/PermissionsScreen.swift).
 *
 * Reachable from Settings → "Open full permissions screen". Gives a
 * full-screen explanation of why each permission is needed so users who
 * previously denied one can re-request with more context than the card row
 * in Settings provides.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
) {
    val vm: PermissionsViewModel = hiltViewModel()
    val settingsVm: SettingsViewModel = hiltViewModel()
    val states by vm.states.collectAsState()
    val ctx = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refreshAfterReturningFromSettings()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_permissions_reliability_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsCard {
                TymePermission.requiredPermissions.forEachIndexed { idx, perm ->
                    val nfcUnavailable = perm == TymePermission.NFC && !vm.isNfcAvailable
                    PermissionRow(
                        permission = perm,
                        granted = states[perm] == true,
                        onGrantClick = { openPermissionIntent(ctx, perm) },
                        unavailable = nfcUnavailable,
                    )
                    if (idx < TymePermission.requiredPermissions.lastIndex) {
                        SettingsCardDivider()
                    }
                }
            }

            SettingsCard(title = "Troubleshooting", elevation = 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { settingsVm.resetBlockingState() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Reset blocking state",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun openPermissionIntent(context: Context, perm: TymePermission) {
    runCatching {
        context.startActivity(PermissionIntents.intentFor(context, perm))
    }
}
