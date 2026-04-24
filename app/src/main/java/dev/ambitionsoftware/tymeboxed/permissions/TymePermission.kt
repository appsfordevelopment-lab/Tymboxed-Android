package dev.ambitionsoftware.tymeboxed.permissions

/**
 * Every Android permission the app cares about, with user-facing copy.
 *
 * [required] is always true for every entry: the intro wizard and session gate
 * require all of these. Devices without NFC hardware treat NFC as satisfied
 * automatically (see [PermissionsCoordinator]).
 */
enum class TymePermission(
    val key: String,
    val title: String,
    val description: String,
    val required: Boolean,
) {
    ACCESSIBILITY(
        key = "accessibility",
        title = "Accessibility",
        description = "Lets Tyme Boxed detect when a blocked app is opened and return you to the home screen.",
        required = true,
    ),
    USAGE_STATS(
        key = "usage_stats",
        title = "App usage access",
        description = "Turn on “App usage access” for Tyme Boxed so it can see which app is in the foreground and enforce blocking. Android will open the list — find Tyme Boxed and allow it.",
        required = true,
    ),
    NOTIFICATIONS(
        key = "notifications",
        title = "Notifications",
        description = "Shows the ongoing focus session notification while blocking is active.",
        required = true,
    ),
    NFC(
        key = "nfc",
        title = "NFC",
        description = "Must be on to use Tyme Boxed with NFC tags and reliable session control.",
        required = true,
    );

    companion object {
        val requiredPermissions: List<TymePermission> = entries.toList()
    }
}
