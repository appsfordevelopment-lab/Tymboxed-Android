package dev.ambitionsoftware.tymeboxed.domain.model

import dev.ambitionsoftware.tymeboxed.data.db.ProfileWithApps
import dev.ambitionsoftware.tymeboxed.data.db.entities.BlockedAppEntity
import dev.ambitionsoftware.tymeboxed.data.db.entities.ProfileEntity
import java.util.UUID

/**
 * Domain model for a blocking profile. Exposes [blockedPackages] as a flat
 * list instead of the row-based Room representation so UI code never has to
 * know about [BlockedAppEntity]. Create via [newDraft] for a new profile or
 * via [ProfileWithApps.toDomain] from a DB read.
 */
data class Profile(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val strategyId: String,
    val strategyData: String? = null,

    val enableStrictMode: Boolean = true,
    val enableLiveActivity: Boolean = false,
    val enableBreaks: Boolean = false,
    val breakTimeInMinutes: Int = 15,

    val reminderTimeSeconds: Int? = null,
    val customReminderMessage: String? = null,

    val physicalUnblockNfcTagId: String? = null,
    val isAllowMode: Boolean = false,
    val isAllowModeDomains: Boolean = false,
    val domains: List<String> = emptyList(),
    val order: Int = 0,
    val accentColorHex: String? = null,

    val schedule: ProfileSchedule? = null,

    val blockedPackages: List<String> = emptyList(),
) {
    companion object {
        fun newDraft(defaultStrategyId: String): Profile {
            val now = System.currentTimeMillis()
            return Profile(
                id = UUID.randomUUID().toString(),
                name = "",
                createdAt = now,
                updatedAt = now,
                strategyId = defaultStrategyId,
            )
        }
    }
}

/**
 * Keeps [strategyId] aligned with [enableBreaks] for the focus-timer family,
 * matching iOS where breaks are controlled by the toggle, not a mismatched
 * [strategyId] with [enableBreaks] left over from a prior save.
 */
fun Profile.normalizedForBreaks(): Profile = when {
    enableBreaks && strategyId == BlockingStrategyId.FOCUS_TIMER ->
        copy(strategyId = BlockingStrategyId.FOCUS_TIMER_BREAK)
    !enableBreaks && strategyId == BlockingStrategyId.FOCUS_TIMER_BREAK ->
        copy(strategyId = BlockingStrategyId.FOCUS_TIMER)
    else -> this
}

fun ProfileEntity.toDomain(blockedPackages: List<String>): Profile = Profile(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    strategyId = strategyId,
    strategyData = strategyData,
    enableStrictMode = enableStrictMode,
    enableLiveActivity = enableLiveActivity,
    enableBreaks = enableBreaks,
    breakTimeInMinutes = breakTimeInMinutes,
    reminderTimeSeconds = reminderTimeSeconds,
    customReminderMessage = customReminderMessage,
    physicalUnblockNfcTagId = physicalUnblockNfcTagId,
    isAllowMode = isAllowMode,
    isAllowModeDomains = isAllowModeDomains,
    domains = domains?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
    order = order,
    accentColorHex = accentColorHex,
    schedule = scheduleJson.decodeProfileSchedule(),
    blockedPackages = blockedPackages,
)

fun ProfileWithApps.toDomain(): Profile =
    profile.toDomain(blockedApps.map { it.packageName })

fun Profile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    strategyId = strategyId,
    strategyData = strategyData,
    enableStrictMode = enableStrictMode,
    enableLiveActivity = enableLiveActivity,
    enableBreaks = enableBreaks,
    breakTimeInMinutes = breakTimeInMinutes,
    reminderTimeSeconds = reminderTimeSeconds,
    customReminderMessage = customReminderMessage,
    physicalUnblockNfcTagId = physicalUnblockNfcTagId,
    isAllowMode = isAllowMode,
    isAllowModeDomains = isAllowModeDomains,
    domains = domains.takeIf { it.isNotEmpty() }?.joinToString(","),
    order = order,
    accentColorHex = accentColorHex,
    scheduleJson = schedule?.toJson(),
)
