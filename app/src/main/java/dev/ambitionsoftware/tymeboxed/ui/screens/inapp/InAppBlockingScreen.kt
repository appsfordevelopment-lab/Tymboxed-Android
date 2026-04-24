package dev.ambitionsoftware.tymeboxed.ui.screens.inapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ambitionsoftware.tymeboxed.R
import dev.ambitionsoftware.tymeboxed.service.inapp.InAppToggleKeys
import dev.ambitionsoftware.tymeboxed.ui.components.SettingsNavigationRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppBlockingScreen(
    onBack: () -> Unit,
) {
    val vm: InAppBlockingViewModel = hiltViewModel()
    val s by vm.state.collectAsState()
    var showLimitDialog by remember { mutableStateOf(false) }

    if (showLimitDialog) {
        DailyLimitDialog(
            current = s.limitMinutes,
            onDismiss = { showLimitDialog = false },
            onPick = { m ->
                vm.setDailyLimit(m)
                showLimitDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.in_app_blocking_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.in_app_limit_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.in_app_limit_card_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.in_app_limit_status_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                if (s.limitMinutes <= 0) {
                                    stringResource(R.string.in_app_no_limit)
                                } else {
                                    stringResource(R.string.in_app_minutes_value, s.limitMinutes)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        Button(
                            onClick = { showLimitDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) { Text(stringResource(R.string.in_app_set_daily_limit)) }
                    }
                }
            }

            InAppAppSection(
                title = "YouTube",
                expandedInit = true,
            ) {
                InAppSwitchRow("Block Shorts", s.blockYtShorts) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_YT_SHORTS, it) }
                InAppSwitchRow("Block video search", s.blockYtSearch) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_YT_SEARCH, it) }
                InAppSwitchRow("Block comments", s.blockYtComments) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_YT_COMMENTS, it) }
                InAppSwitchRow("Block picture-in-picture mode", s.blockYtPip) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_YT_PIP, it) }
            }

            InAppAppSection("Instagram", expandedInit = true) {
                InAppSwitchRow("Block Reels", s.blockIgReels) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_IG_REELS, it) }
                InAppSwitchRow("Block Explore tab", s.blockIgExplore) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_IG_EXPLORE, it) }
                InAppSwitchRow("Block Search", s.blockIgSearch) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_IG_SEARCH, it) }
                InAppSwitchRow("Block comments", s.blockIgComments) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_IG_COMMENTS, it) }
                InAppSwitchRow("Block Stories", s.blockIgStories) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_IG_STORIES, it) }
            }

            InAppAppSection("X / Twitter", expandedInit = false) {
                InAppSwitchRow("Block Home", s.blockXHome) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_X_HOME, it) }
                InAppSwitchRow("Block Search", s.blockXSearch) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_X_SEARCH, it) }
                InAppSwitchRow("Block Grok", s.blockXGrok) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_X_GROK, it) }
                InAppSwitchRow("Block Notifications", s.blockXNotifications) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_X_NOTIFICATIONS, it) }
            }

            InAppAppSection("Snapchat", expandedInit = false) {
                InAppSwitchRow("Block Map", s.blockSnapMap) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_SNAP_MAP, it) }
                InAppSwitchRow("Block Stories", s.blockSnapStories) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_SNAP_STORIES, it) }
                InAppSwitchRow("Block Spotlight", s.blockSnapSpotlight) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_SNAP_SPOTLIGHT, it) }
                InAppSwitchRow("Block Following", s.blockSnapFollowing) { vm.setToggle(InAppToggleKeys.KEY_BLOCK_SNAP_FOLLOWING, it) }
            }

            Text(
                stringResource(R.string.in_app_footer_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun InAppAppSection(
    title: String,
    expandedInit: Boolean,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(expandedInit) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
            )
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InAppSwitchRow(
    label: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
        )
    }
}

@Composable
private fun DailyLimitDialog(
    current: Int,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit,
) {
    val options = listOf(0, 15, 30, 60, 90, 120, 180)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.in_app_set_daily_limit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                options.forEach { m ->
                    val label = if (m == 0) {
                        stringResource(R.string.in_app_no_limit)
                    } else {
                        stringResource(R.string.in_app_minutes_value, m)
                    }
                    Text(
                        label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPick(m)
                            }
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (m == current) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
    )
}

/** Settings screen row: navigates to [InAppBlockingScreen]. */
@Composable
fun InAppBlockingSettingsRow(
    onClick: () -> Unit,
) {
    SettingsNavigationRow(
        title = stringResource(R.string.in_app_blocking_title),
        subtitle = stringResource(R.string.in_app_blocking_subtitle),
        icon = Icons.Filled.Tune,
        onClick = onClick,
    )
}
