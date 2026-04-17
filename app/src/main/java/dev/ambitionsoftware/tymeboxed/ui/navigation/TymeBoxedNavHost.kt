package dev.ambitionsoftware.tymeboxed.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences
import dev.ambitionsoftware.tymeboxed.ui.screens.home.HomeScreen
import dev.ambitionsoftware.tymeboxed.ui.screens.intro.IntroScreen
import dev.ambitionsoftware.tymeboxed.ui.screens.permissions.PermissionsScreen
import dev.ambitionsoftware.tymeboxed.ui.screens.profile.ProfileEditScreen
import dev.ambitionsoftware.tymeboxed.ui.screens.settings.SettingsScreen

/**
 * Top-level navigation graph. Single-activity app — this is hosted inside
 * `MainActivity` below [TbTheme].
 *
 * Initial destination is [Routes.INTRO] on first launch (`introCompleted == false`)
 * and [Routes.HOME] otherwise. We read the flag synchronously once to pick the
 * starting route; the rest of the app uses Flow-based observation.
 */
@Composable
fun TymeBoxedNavHost(
    prefs: AppPreferences,
) {
    val navController = rememberNavController()
    val introCompleted by prefs.introCompleted.collectAsState(initial = null)

    // Wait until we know the flag before deciding the start destination.
    // While null we render an empty graph.
    if (introCompleted == null) return

    val startRoute = if (introCompleted == true) Routes.HOME else Routes.INTRO

    NavHost(
        navController = navController,
        startDestination = startRoute,
    ) {
        composable(Routes.INTRO) {
            IntroScreen(
                prefs = prefs,
                onIntroComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.INTRO) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onCreateProfile = { navController.navigate(Routes.profileEdit()) },
                onEditProfile = { id -> navController.navigate(Routes.profileEdit(id)) },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenFullPermissions = { navController.navigate(Routes.PERMISSIONS) },
            )
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.PROFILE_EDIT,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: "new"
            ProfileEditScreen(
                profileId = profileId,
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { newId ->
                    navController.navigate(Routes.profileEdit(newId)) {
                        popUpTo(Routes.profileEdit(profileId)) { inclusive = true }
                    }
                },
            )
        }
    }
}
