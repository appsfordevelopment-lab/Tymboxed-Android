package dev.ambitionsoftware.tymeboxed.permissions

/**
 * Every Android permission the app cares about, with user-facing copy.
 *
 * iOS needs one permission (Family Controls); Android needs 7. Each entry
 * carries its own title/description so the UI can render a unified list in
 * the intro wizard and in Settings > Permissions.
 *
 * [required] marks whether the permission is a hard gate for starting any
 * focus session. Optional permissions (Exact Alarms, Battery, NFC) are only
 * needed by specific strategies and the UI lets the user skip them.
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
        title = "Usage Access",
        description = "Reads which app is in the foreground so blocking can react instantly.",
        required = true,
    ),
    NOTIFICATIONS(
        key = "notifications",
        title = "Notifications",
        description = "Shows the ongoing focus session notification while blocking is active.",
        required = true,
    ),
    OVERLAY(
        key = "overlay",
        title = "Display over other apps",
        description = "Optional. Blocking uses a full-screen activity; enable this only if a future feature needs a system overlay.",
        required = false,
    ),
    EXACT_ALARMS(
        key = "exact_alarms",
        title = "Exact alarms",
        description = "Keeps timer-based focus sessions accurate to the second.",
        required = false,
    ),
    BATTERY_OPTIMIZATIONS(
        key = "battery",
        title = "Unrestricted battery",
        description = "Stops Android from killing the blocking engine in the background.",
        required = false,
    ),
    NFC(
        key = "nfc",
        title = "NFC",
        description = "Required for Tyme Boxed Mode tag scans. Optional for manual strategies.",
        required = false,
    );

    companion object {
        val requiredPermissions: List<TymePermission> = entries.filter { it.required }
        val optionalPermissions: List<TymePermission> = entries.filterNot { it.required }
    }
}
