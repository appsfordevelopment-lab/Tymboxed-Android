package dev.ambitionsoftware.tymeboxed.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Shared ViewModel for the intro wizard's permissions step, the standalone
 * [PermissionsScreen], and the Settings → Permissions card. Wrapping
 * [PermissionsCoordinator] here means there's exactly one place that calls
 * `refresh()` on app resume, and the three UI surfaces stay in sync.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val coordinator: PermissionsCoordinator,
) : ViewModel() {

    val states: StateFlow<Map<TymePermission, Boolean>> = coordinator.states.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap(),
    )

    val allRequiredGranted: StateFlow<Boolean> = coordinator.allRequiredGranted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false,
    )

    /** True when the device has NFC hardware. */
    val isNfcAvailable: Boolean get() = coordinator.isNfcHardwareAvailable

    /** Call from `ON_RESUME` so the settings deep-link updates re-check. */
    fun refresh() = coordinator.refresh()
}
