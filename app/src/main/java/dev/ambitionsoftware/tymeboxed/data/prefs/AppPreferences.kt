package dev.ambitionsoftware.tymeboxed.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore-backed preferences — replaces iOS @AppStorage / UserDefaults. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tymeboxed_prefs",
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val themeColorName: Flow<String> =
        context.dataStore.data.map { it[KEY_THEME] ?: DEFAULT_THEME_NAME }

    val introCompleted: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_INTRO_COMPLETED] ?: false }

    /** When false, the home Activity heatmap is hidden. */
    val activityChartVisible: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_ACTIVITY_CHART_VISIBLE] ?: true }

    /** One of [ActivityChartType] string constants. */
    val activityChartType: Flow<String> =
        context.dataStore.data.map { it[KEY_ACTIVITY_CHART_TYPE] ?: ActivityChartType.MONTHLY }

    suspend fun setThemeColorName(name: String) {
        context.dataStore.edit { it[KEY_THEME] = name }
    }

    suspend fun setIntroCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_INTRO_COMPLETED] = completed }
    }

    suspend fun setActivityChartVisible(visible: Boolean) {
        context.dataStore.edit { it[KEY_ACTIVITY_CHART_VISIBLE] = visible }
    }

    suspend fun setActivityChartType(type: String) {
        context.dataStore.edit { it[KEY_ACTIVITY_CHART_TYPE] = type }
    }

    companion object {
        /** Matches the iOS default in `ThemeManager.swift`. */
        const val DEFAULT_THEME_NAME = "Warm Sandstone"

        private val KEY_THEME = stringPreferencesKey("theme_color_name")
        private val KEY_INTRO_COMPLETED = booleanPreferencesKey("intro_completed")
        private val KEY_ACTIVITY_CHART_VISIBLE = booleanPreferencesKey("activity_chart_visible")
        private val KEY_ACTIVITY_CHART_TYPE = stringPreferencesKey("activity_chart_type")
    }
}

/** Persisted values for the home Activity chart (Manage sheet). */
object ActivityChartType {
    const val FOUR_WEEK = "four_week"
    const val WEEKLY = "weekly"
    const val MONTHLY = "monthly"
}
