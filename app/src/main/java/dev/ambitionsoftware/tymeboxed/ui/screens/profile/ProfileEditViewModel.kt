package dev.ambitionsoftware.tymeboxed.ui.screens.profile

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.domain.model.BlockingStrategyId
import dev.ambitionsoftware.tymeboxed.domain.model.Profile
import dev.ambitionsoftware.tymeboxed.domain.model.ProfileSchedule
import dev.ambitionsoftware.tymeboxed.data.repository.ProfileRepository
import dev.ambitionsoftware.tymeboxed.data.repository.SessionRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String,
)

data class ProfileEditUiState(
    val isNew: Boolean = true,
    /** `true` once an existing profile has been read from the DB (or immediately for new profiles). */
    val profileReady: Boolean = false,
    val name: String = "",
    val strategyId: String = BlockingStrategyId.DEFAULT,
    val timerMinutes: Int = 25,
    val enableStrictMode: Boolean = true,
    val enableBreaks: Boolean = false,
    val breakTimeInMinutes: Int = 15,
    val enableLiveActivity: Boolean = false,
    val enableReminder: Boolean = false,
    val reminderTimeMinutes: Int = 15,
    val customReminderMessage: String = "",
    val isAllowMode: Boolean = false,
    val isAllowModeDomains: Boolean = false,
    val domains: List<String> = emptyList(),
    val schedule: ProfileSchedule = ProfileSchedule.inactive(),
    val blockedPackages: Set<String> = emptySet(),
    val installedApps: List<InstalledApp> = emptyList(),
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val errorMessage: String? = null,
    /** True when a non-ended session is using this profile — delete must be blocked. */
    val isActiveSessionForThisProfile: Boolean = false,
)

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val profileId: String = savedStateHandle["profileId"] ?: "new"
    private val isNew: Boolean = profileId == "new"

    private val _state = MutableStateFlow(
        ProfileEditUiState(isNew = isNew, profileReady = isNew),
    )
    val state: StateFlow<ProfileEditUiState> = _state.asStateFlow()

    private var loadedProfile: Profile? = null

    private val _pendingNavigationProfileId = MutableStateFlow<String?>(null)
    val pendingNavigationProfileId: StateFlow<String?> = _pendingNavigationProfileId.asStateFlow()

    init {
        loadInstalledApps()
        if (!isNew) {
            loadProfile()
            viewModelScope.launch {
                sessionRepository.activeSession.collect { session ->
                    val locked = session?.profileId == profileId
                    _state.update { it.copy(isActiveSessionForThisProfile = locked) }
                }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = profileRepository.findById(profileId) ?: return@launch
            loadedProfile = profile
            val timerMinutes = profile.strategyData?.toIntOrNull() ?: 25
            _state.update {
                it.copy(
                    isNew = false,
                    profileReady = true,
                    name = profile.name,
                    strategyId = profile.strategyId,
                    timerMinutes = timerMinutes,
                    enableStrictMode = profile.enableStrictMode,
                    enableBreaks = profile.enableBreaks,
                    breakTimeInMinutes = profile.breakTimeInMinutes,
                    enableLiveActivity = profile.enableLiveActivity,
                    enableReminder = profile.reminderTimeSeconds != null,
                    reminderTimeMinutes = (profile.reminderTimeSeconds ?: 900) / 60,
                    customReminderMessage = profile.customReminderMessage ?: "",
                    isAllowMode = profile.isAllowMode,
                    isAllowModeDomains = profile.isAllowModeDomains,
                    domains = profile.domains,
                    schedule = profile.schedule ?: ProfileSchedule.inactive(),
                    blockedPackages = profile.blockedPackages.toSet(),
                )
            }
        }
    }

    /**
     * Loads all apps that have a launcher intent — this catches user-installed
     * apps AND visible system apps (Calculator, Camera, etc.) that would be
     * useful to block. Falls back to non-system filter if the query fails.
     */
    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = appContext.packageManager
                val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= 33) {
                    pm.queryIntentActivities(
                        launcherIntent,
                        PackageManager.ResolveInfoFlags.of(0),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(launcherIntent, 0)
                }
                resolveInfos
                    .mapNotNull { it.activityInfo }
                    .distinctBy { it.packageName }
                    .filter { it.packageName != appContext.packageName }
                    .map { info ->
                        InstalledApp(
                            packageName = info.packageName,
                            label = info.loadLabel(pm).toString(),
                        )
                    }
                    .sortedBy { it.label.lowercase() }
            }
            _state.update { it.copy(installedApps = apps) }
        }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, errorMessage = null) }
    }

    fun onStrategyChange(strategyId: String) {
        _state.update { s ->
            val enableBreaks = when (strategyId) {
                BlockingStrategyId.FOCUS_TIMER_BREAK -> true
                else -> false
            }
            s.copy(strategyId = strategyId, enableBreaks = enableBreaks, errorMessage = null)
        }
    }

    fun onTimerMinutesChange(minutes: Int) {
        _state.update { it.copy(timerMinutes = minutes.coerceIn(1, 480)) }
    }

    fun onStrictModeChange(enabled: Boolean) {
        _state.update { it.copy(enableStrictMode = enabled) }
    }

    fun onBreaksChange(enabled: Boolean) {
        _state.update { s ->
            var sid = s.strategyId
            when {
                enabled && s.strategyId == BlockingStrategyId.FOCUS_TIMER ->
                    sid = BlockingStrategyId.FOCUS_TIMER_BREAK
                !enabled && s.strategyId == BlockingStrategyId.FOCUS_TIMER_BREAK ->
                    sid = BlockingStrategyId.FOCUS_TIMER
            }
            s.copy(enableBreaks = enabled, strategyId = sid)
        }
    }

    fun onBreakTimeChange(minutes: Int) {
        _state.update { it.copy(breakTimeInMinutes = minutes) }
    }

    fun onLiveActivityChange(enabled: Boolean) {
        _state.update { it.copy(enableLiveActivity = enabled) }
    }

    fun onReminderChange(enabled: Boolean) {
        _state.update { it.copy(enableReminder = enabled) }
    }

    fun onReminderTimeChange(minutes: Int) {
        _state.update { it.copy(reminderTimeMinutes = minutes.coerceIn(1, 999)) }
    }

    fun onReminderMessageChange(message: String) {
        _state.update { it.copy(customReminderMessage = message.take(178)) }
    }

    fun onAllowModeChange(enabled: Boolean) {
        _state.update { it.copy(isAllowMode = enabled, blockedPackages = emptySet()) }
    }

    fun onAllowModeDomainsChange(enabled: Boolean) {
        _state.update { it.copy(isAllowModeDomains = enabled, domains = emptyList()) }
    }

    fun onToggleApp(packageName: String) {
        _state.update { s ->
            val updated = s.blockedPackages.toMutableSet()
            if (updated.contains(packageName)) updated.remove(packageName)
            else updated.add(packageName)
            s.copy(blockedPackages = updated)
        }
    }

    fun onAddDomain(domain: String) {
        val trimmed = domain.trim().lowercase()
        if (trimmed.isBlank()) return
        _state.update { s ->
            if (s.domains.contains(trimmed)) s
            else s.copy(domains = s.domains + trimmed)
        }
    }

    fun onRemoveDomain(domain: String) {
        _state.update { s -> s.copy(domains = s.domains - domain) }
    }

    fun updateSchedule(schedule: ProfileSchedule) {
        val normalized = if (schedule.isActive) {
            schedule.copy(updatedAt = System.currentTimeMillis())
        } else {
            ProfileSchedule.inactive()
        }
        _state.update { it.copy(schedule = normalized) }
    }

    fun save() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Profile name is required") }
            return
        }
        if (current.blockedPackages.isEmpty() && current.domains.isEmpty()) {
            _state.update { it.copy(errorMessage = "Select at least one app or domain") }
            return
        }
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val base = loadedProfile ?: Profile.newDraft(current.strategyId)
                val profile = base.copy(
                    name = current.name.trim(),
                    strategyId = current.strategyId,
                    strategyData = if (current.strategyId in listOf(
                            BlockingStrategyId.FOCUS_TIMER,
                            BlockingStrategyId.FOCUS_TIMER_BREAK,
                        )
                    ) current.timerMinutes.toString() else null,
                    enableStrictMode = current.enableStrictMode,
                    enableBreaks = current.enableBreaks,
                    breakTimeInMinutes = current.breakTimeInMinutes,
                    enableLiveActivity = current.enableLiveActivity,
                    reminderTimeSeconds = if (current.enableReminder) {
                        current.reminderTimeMinutes * 60
                    } else null,
                    customReminderMessage = current.customReminderMessage.ifBlank { null },
                    isAllowMode = current.isAllowMode,
                    isAllowModeDomains = current.isAllowModeDomains,
                    domains = current.domains,
                    schedule = current.schedule.takeIf { it.isActive },
                    blockedPackages = current.blockedPackages.toList(),
                )
                profileRepository.save(profile)
                _state.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save: ${e.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Snapshot for the insights overlay (same profile id as on disk; name reflects the text field).
     */
    fun profileForInsights(): Profile? {
        if (isNew) return null
        val base = loadedProfile ?: return null
        val s = _state.value
        return base.copy(name = s.name.trim().ifBlank { base.name })
    }

    fun consumePendingNavigation() {
        _pendingNavigationProfileId.value = null
    }

    fun duplicateProfile() {
        if (isNew) return
        val current = _state.value
        val base = loadedProfile ?: return
        if (current.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Profile name is required") }
            return
        }
        if (current.blockedPackages.isEmpty() && current.domains.isEmpty()) {
            _state.update { it.copy(errorMessage = "Select at least one app or domain") }
            return
        }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val newId = UUID.randomUUID().toString()
                val copy = base.copy(
                    id = newId,
                    name = current.name.trim() + " Copy",
                    createdAt = now,
                    updatedAt = now,
                    strategyId = current.strategyId,
                    strategyData = if (current.strategyId in listOf(
                            BlockingStrategyId.FOCUS_TIMER,
                            BlockingStrategyId.FOCUS_TIMER_BREAK,
                        )
                    ) {
                        current.timerMinutes.toString()
                    } else {
                        null
                    },
                    enableStrictMode = current.enableStrictMode,
                    enableBreaks = current.enableBreaks,
                    breakTimeInMinutes = current.breakTimeInMinutes,
                    enableLiveActivity = current.enableLiveActivity,
                    reminderTimeSeconds = if (current.enableReminder) {
                        current.reminderTimeMinutes * 60
                    } else {
                        null
                    },
                    customReminderMessage = current.customReminderMessage.ifBlank { null },
                    isAllowMode = current.isAllowMode,
                    isAllowModeDomains = current.isAllowModeDomains,
                    domains = current.domains,
                    schedule = current.schedule.takeIf { it.isActive },
                    blockedPackages = current.blockedPackages.toList(),
                )
                profileRepository.save(copy)
                _pendingNavigationProfileId.value = newId
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = "Failed to duplicate: ${e.localizedMessage}")
                }
            }
        }
    }

    fun delete() {
        if (isNew) return
        if (_state.value.isActiveSessionForThisProfile) {
            _state.update {
                it.copy(errorMessage = "End the focus session before deleting this profile.")
            }
            return
        }
        _state.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            try {
                profileRepository.delete(profileId)
                _state.update { it.copy(isDeleting = false, deletedSuccessfully = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "Failed to delete: ${e.localizedMessage}",
                    )
                }
            }
        }
    }
}
