package dev.ambitionsoftware.tymeboxed.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
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
                usageAccessSettingsIntent(context)

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

    /**
     * Opens the “App usage access” / special-access screen. Many devices support
     * `package:` [Uri] so the user lands on the right row for this app; if no
     * activity resolves, fall back to the generic usage-access page.
     */
    private fun usageAccessSettingsIntent(context: Context): Intent {
        val withPkg = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (withPkg.resolveActivity(context.packageManager) != null) {
            withPkg
        } else {
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).clearTop()
        }
    }
}
