package dev.ambitionsoftware.tymeboxed.ui.navigation

/**
 * All Navigation Compose routes in one place. Single-activity app — the
 * [TymeBoxedNavHost] swaps between these composables inside `MainActivity`.
 */
object Routes {
    const val INTRO = "intro"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val IN_APP_BLOCKING = "in_app_blocking"
    const val PERMISSIONS = "permissions"

    /** Placeholder for Phase 2 — profile create / edit. */
    const val PROFILE_EDIT = "profile_edit/{profileId}"
    fun profileEdit(profileId: String = "new") = "profile_edit/$profileId"
}
