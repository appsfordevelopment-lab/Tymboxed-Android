package dev.ambitionsoftware.tymeboxed.ui.screens.inapp

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ambitionsoftware.tymeboxed.service.inapp.InAppToggleKeys
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class InAppBlockingViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
) : ViewModel() {

    private val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(InAppUiState.from(app))
    val state: StateFlow<InAppUiState> = _state.asStateFlow()

    fun setToggle(key: String, value: Boolean) = viewModelScope.launch {
        prefs.edit { putBoolean(key, value) }
        _state.update { InAppUiState.from(app) }
    }

    companion object {
        private const val PREFS = "tymeboxed_inapp_toggles"
    }
}

data class InAppUiState(
    val blockYtShorts: Boolean,
    val blockYtSearch: Boolean,
    val blockYtComments: Boolean,
    val blockYtPip: Boolean,
    val blockIgReels: Boolean,
    val blockIgExplore: Boolean,
    val blockIgSearch: Boolean,
    val blockIgStories: Boolean,
    val blockIgComments: Boolean,
    val blockXHome: Boolean,
    val blockXSearch: Boolean,
    val blockXGrok: Boolean,
    val blockXNotifications: Boolean,
    val blockSnapMap: Boolean,
    val blockSnapStories: Boolean,
    val blockSnapSpotlight: Boolean,
    val blockSnapFollowing: Boolean,
) {
    companion object {
        fun from(ctx: android.content.Context): InAppUiState {
            val p = ctx.getSharedPreferences("tymeboxed_inapp_toggles", android.content.Context.MODE_PRIVATE)
            return InAppUiState(
                blockYtShorts = p.getBoolean(InAppToggleKeys.KEY_BLOCK_YT_SHORTS, false),
                blockYtSearch = p.getBoolean(InAppToggleKeys.KEY_BLOCK_YT_SEARCH, false),
                blockYtComments = p.getBoolean(InAppToggleKeys.KEY_BLOCK_YT_COMMENTS, false),
                blockYtPip = p.getBoolean(InAppToggleKeys.KEY_BLOCK_YT_PIP, false),
                blockIgReels = p.getBoolean(InAppToggleKeys.KEY_BLOCK_IG_REELS, false),
                blockIgExplore = p.getBoolean(InAppToggleKeys.KEY_BLOCK_IG_EXPLORE, false),
                blockIgSearch = p.getBoolean(InAppToggleKeys.KEY_BLOCK_IG_SEARCH, false),
                blockIgStories = p.getBoolean(InAppToggleKeys.KEY_BLOCK_IG_STORIES, false),
                blockIgComments = p.getBoolean(InAppToggleKeys.KEY_BLOCK_IG_COMMENTS, false),
                blockXHome = p.getBoolean(InAppToggleKeys.KEY_BLOCK_X_HOME, false),
                blockXSearch = p.getBoolean(InAppToggleKeys.KEY_BLOCK_X_SEARCH, false),
                blockXGrok = p.getBoolean(InAppToggleKeys.KEY_BLOCK_X_GROK, false),
                blockXNotifications = p.getBoolean(InAppToggleKeys.KEY_BLOCK_X_NOTIFICATIONS, false),
                blockSnapMap = p.getBoolean(InAppToggleKeys.KEY_BLOCK_SNAP_MAP, false),
                blockSnapStories = p.getBoolean(InAppToggleKeys.KEY_BLOCK_SNAP_STORIES, false),
                blockSnapSpotlight = p.getBoolean(InAppToggleKeys.KEY_BLOCK_SNAP_SPOTLIGHT, false),
                blockSnapFollowing = p.getBoolean(InAppToggleKeys.KEY_BLOCK_SNAP_FOLLOWING, false),
            )
        }
    }
}
