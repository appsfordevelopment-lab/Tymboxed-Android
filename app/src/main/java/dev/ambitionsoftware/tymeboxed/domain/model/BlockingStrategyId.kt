package dev.ambitionsoftware.tymeboxed.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Canonical string identifiers for blocking strategies. These values are
 * persisted in [dev.ambitionsoftware.tymeboxed.data.db.entities.ProfileEntity.strategyId],
 * so treat them as an append-only contract — renaming breaks existing
 * profiles on-device.
 *
 * Mirrors the iOS strategy `id` pattern from
 * `TymeBoxed/Models/Strategies/BlockingStrategy.swift`, NFC strategies only.
 */
object BlockingStrategyId {
    const val MANUAL = "manual"
    const val NFC_UNLOCK = "nfc_unlock"
    const val NFC_MANUAL_START = "nfc_manual_start"
    const val FOCUS_TIMER = "focus_timer"
    const val FOCUS_TIMER_BREAK = "focus_timer_break"

    /** Default strategy for a brand-new profile (matches iOS `NFCBlockingStrategy.id`). */
    const val DEFAULT: String = NFC_UNLOCK

    val all: List<String> = listOf(
        NFC_UNLOCK, NFC_MANUAL_START, FOCUS_TIMER, FOCUS_TIMER_BREAK, MANUAL,
    )
}

/** Tag labels shown as capsules on each strategy row, matching iOS. */
enum class StrategyTag(val label: String) {
    DEVICE("Device"),
    TIMER("Timer"),
    MANUAL("Manual"),
    BREAK("Break"),
}

/**
 * Display metadata for a blocking strategy — icon, color, name, tags, and
 * description. Matches iOS strategy presentation in the profile editor.
 */
data class StrategyInfo(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val color: Color,
    val tags: List<StrategyTag> = emptyList(),
    val hasTimer: Boolean = false,
    val hidden: Boolean = false,
)

/** All strategies — visible ones appear in the profile editor picker. */
val availableStrategies: List<StrategyInfo> = listOf(
    StrategyInfo(
        id = BlockingStrategyId.NFC_UNLOCK,
        name = "Tyme Boxed Mode",
        description = "Lock and unlock using your device.",
        icon = "nfc",
        color = Color(0xFFF5A623), // yellow
        tags = listOf(StrategyTag.DEVICE),
    ),
    StrategyInfo(
        id = BlockingStrategyId.NFC_MANUAL_START,
        name = "Tyme Boxed + Manual Start",
        description = "Lock manually, then scan the device to unlock.",
        icon = "nfc",
        color = Color(0xFFF5A623), // yellow
        tags = listOf(StrategyTag.DEVICE, StrategyTag.MANUAL),
    ),
    StrategyInfo(
        id = BlockingStrategyId.FOCUS_TIMER,
        name = "Focus Session",
        description = "Set a focus duration, then scan the device to end early.",
        icon = "timer",
        color = Color(0xFF4CD9AC), // mint
        tags = listOf(StrategyTag.DEVICE, StrategyTag.TIMER),
        hasTimer = true,
    ),
    StrategyInfo(
        id = BlockingStrategyId.FOCUS_TIMER_BREAK,
        name = "Focus session with Break",
        description = "Set a break duration, scan once for break, and scan again to fully stop.",
        icon = "pause",
        color = Color(0xFFFF9500), // orange
        tags = listOf(StrategyTag.DEVICE, StrategyTag.TIMER, StrategyTag.BREAK),
        hasTimer = true,
    ),
    StrategyInfo(
        id = BlockingStrategyId.MANUAL,
        name = "Manual",
        description = "Start and stop blocking directly in the app.",
        icon = "touch_app",
        color = Color(0xFF888888),
        hidden = true,
    ),
)

/** Lookup a strategy's display info by ID. */
fun strategyInfoById(id: String): StrategyInfo? =
    availableStrategies.find { it.id == id }
