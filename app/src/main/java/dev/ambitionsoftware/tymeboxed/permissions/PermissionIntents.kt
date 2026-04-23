package dev.ambitionsoftware.tymeboxed.permissions

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Deep-link intents for every [TymePermission]. Each returns the Intent that,
 * when launched, lands the user on the correct Android system settings page
 * to grant the permission in question.
 *
 * POST_NOTIFICATIONS is the exception: on Android 13+ it uses the runtime
 * permission dialog (handled in the Compose layer with ActivityResultContracts),
 * so [intentFor] returns the app's notification settings page as a fallback
 * for users who previously denied the prompt.
 */
object PermissionIntents {

    fun intentFor(context: Context, perm: TymePermission): Intent {
        return when (perm) {
            TymePermission.ACCESSIBILITY ->
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).clearTop()

            TymePermission.USAGE_STATS ->
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).clearTop()

            TymePermission.NOTIFICATIONS ->
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            TymePermission.NFC ->
                Intent(Settings.ACTION_NFC_SETTINGS).clearTop()
        }
    }

    private fun Intent.clearTop(): Intent {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return this
    }
}
