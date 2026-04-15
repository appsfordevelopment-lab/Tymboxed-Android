package dev.ambitionsoftware.tymeboxed.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Core app-blocking engine, modeled after Switchly's SwitchlyAccessibilityService.
 *
 * Listens for multiple accessibility event types to reliably detect
 * the foreground app. When a blocked package is detected, launches a
 * full-screen [BlockerActivity] overlay that says "This app is blocked
 * by Tyme Boxed" (instead of force-closing the app).
 *
 * Key robustness features (aligned with Switchly):
 * - Programmatic `serviceInfo` configuration in `onServiceConnected`
 * - 1-second heartbeat tick for continuous enforcement + keepalive
 * - Polls `rootInActiveWindow` as a fallback for OEMs with unreliable transitions
 * - Polls `UsageStatsManager` events as a secondary fallback
 * - Event deduplication to reduce overhead under event storms
 * - Screen-off / keyguard-locked awareness
 * - Shows overlay instead of killing the blocked app
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    /** Background thread for UsageStats queries (the binder can stall on some OEMs). */
    private var usageWorkerThread: HandlerThread? = null
    private var usageWorker: Handler? = null
    @Volatile private var topRefreshInFlight = false

    private lateinit var pm: PowerManager
    private var km: KeyguardManager? = null

    // Current foreground package as tracked by events + polling
    @Volatile private var currentTopPkg: String? = null

    // Event deduplication
    private var lastEventPkg: String? = null
    private var lastEventAt: Long = 0L
    private var lastEventType: Int = 0
    private var lastTransitionAt: Long = 0L

    // Per-package enforcement cadence (prevents duplicate blocks during event storms)
    private val lastEnforceAtByPkg = HashMap<String, Long>()

    // Debounce blocker overlay launches
    private val lastBlockShownAt = HashMap<String, Long>()
    private var lastGlobalBlockTs: Long = 0L

    private val BLOCK_SHOWN_COOLDOWN_MS = 800L

    // UsageEvents foreground refresh cadence
    private var lastTopRefreshAt: Long = 0L
    private val TOP_REFRESH_INTERVAL_MS = 3_000L

    // -----------------------------------------------------------------------
    // Service lifecycle
    // -----------------------------------------------------------------------

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "AppBlockerAccessibilityService connected.")

        pm = getSystemService(POWER_SERVICE) as PowerManager
        km = getSystemService(KeyguardManager::class.java)

        // Programmatically configure — overrides XML so we can be sure it's correct.
        // This matches Switchly's onServiceConnected configuration.
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_SCROLLED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 0
            flags =
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }

        // Start the background thread for UsageStats polling
        usageWorkerThread?.quitSafely()
        usageWorkerThread = HandlerThread("tymeboxed-usage-worker").apply { start() }
        usageWorker = Handler(usageWorkerThread!!.looper)
        topRefreshInFlight = false

        // Mark alive and start heartbeat tick
        ActiveBlockingState.markHeartbeat()
        handler.removeCallbacks(tick)
        handler.postDelayed(tick, 1_000L)
    }

    override fun onInterrupt() {
        // no-op
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        usageWorkerThread?.quitSafely()
        usageWorkerThread = null
        usageWorker = null
        topRefreshInFlight = false

        ActiveBlockingState.markDisconnected()
        Log.i(TAG, "AppBlockerAccessibilityService destroyed.")
        super.onDestroy()
    }

    // -----------------------------------------------------------------------
    // Heartbeat tick — runs every 1 second
    // -----------------------------------------------------------------------

    private val tick = object : Runnable {
        override fun run() {
            try {
                ActiveBlockingState.markHeartbeat()

                // Re-enforce blocking on the current foreground package.
                // This catches: schedule boundary changes, profile edits while
                // user is inside a blocked app, and OEMs that miss transitions.
                enforceCurrentForeground()

                // Poll rootInActiveWindow as a secondary foreground signal
                pollActiveWindowPackage()

                // Poll UsageEvents as a tertiary fallback
                refreshTopPackageViaUsageEvents()
            } catch (e: Throwable) {
                Log.w(TAG, "Tick error: ${e.message}")
            }
            handler.postDelayed(this, 1_000L)
        }
    }

    /**
     * Re-check the current foreground app. Covers the case where a profile
     * changes or a schedule boundary flips while the user is already inside
     * a blocked app (no new accessibility event is emitted in that case).
     */
    private fun enforceCurrentForeground() {
        val pkg = currentTopPkg ?: return
        if (pkg.isBlank() || pkg == packageName) return
        maybeBlock(pkg)
    }

    /**
     * Some apps produce very few accessibility events. Poll the active window
     * as a reliable secondary signal for the true foreground package.
     */
    private fun pollActiveWindowPackage() {
        val rootPkg = try {
            rootInActiveWindow?.packageName?.toString()
        } catch (_: Throwable) {
            null
        }
        if (rootPkg.isNullOrBlank() || rootPkg == packageName) return
        if (rootPkg != currentTopPkg) {
            currentTopPkg = rootPkg
            maybeBlock(rootPkg)
        }
    }

    /**
     * Periodically resolve the foreground app via UsageStatsManager.
     * This is the most reliable cross-device signal (catches OEMs that
     * miss accessibility transitions, especially when switching via Recents).
     */
    private fun refreshTopPackageViaUsageEvents() {
        val now = System.currentTimeMillis()
        if (now - lastTopRefreshAt < TOP_REFRESH_INTERVAL_MS) return
        if (topRefreshInFlight) return

        lastTopRefreshAt = now
        topRefreshInFlight = true

        val start = (now - 10_000L).coerceAtLeast(0L)
        usageWorker?.post {
            val top = try {
                resolveTopPackageFromUsageEvents(start, now)
            } catch (_: Throwable) {
                null
            }
            topRefreshInFlight = false
            if (top.isNullOrBlank()) return@post

            handler.post {
                if (top != currentTopPkg && top != packageName) {
                    currentTopPkg = top
                    maybeBlock(top)
                }
            }
        } ?: run {
            topRefreshInFlight = false
        }
    }

    private fun resolveTopPackageFromUsageEvents(start: Long, end: Long): String? {
        val usm = getSystemService(USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val events = try {
            usm.queryEvents(start, end)
        } catch (_: SecurityException) {
            return null
        }
        val e = UsageEvents.Event()
        var lastPkg: String? = null
        var lastTs = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (e.timeStamp >= lastTs && !e.packageName.isNullOrBlank()) {
                    lastTs = e.timeStamp
                    lastPkg = e.packageName
                }
            }
        }
        return lastPkg
    }

    // -----------------------------------------------------------------------
    // Accessibility event handling
    // -----------------------------------------------------------------------

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Refresh heartbeat on every event
        ActiveBlockingState.markHeartbeat()

        val pkg = event?.packageName?.toString()?.trim().orEmpty()
        if (pkg.isBlank()) return
        if (pkg == packageName) return  // avoid loops

        val now = System.currentTimeMillis()
        val type = event?.eventType ?: 0

        // ── Event deduplication ──
        // Identical WINDOW_CONTENT_CHANGED from the same package within 100ms → skip
        if (pkg == lastEventPkg) {
            val dt = now - lastEventAt
            if (type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && dt < 100L) return
            if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                type == lastEventType && dt < 50L
            ) return
        }

        lastEventPkg = pkg
        lastEventAt = now
        lastEventType = type

        // Update foreground tracking
        currentTopPkg = pkg

        val isTransition =
            type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                type == AccessibilityEvent.TYPE_WINDOWS_CHANGED

        if (isTransition) {
            lastTransitionAt = now
            maybeBlock(pkg)
            return
        }

        // For content/text/scroll/click events, enforce blocking shortly after transitions
        // to close the "one-frame flash" window where the app is visible before overlay shows
        if (type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (now - lastTransitionAt <= 500L) {
                maybeBlock(pkg)
            }
        }
    }

    // -----------------------------------------------------------------------
    // Blocking logic
    // -----------------------------------------------------------------------

    /**
     * Check whether [pkg] should be blocked and act on it.
     * Instead of force-closing, launches the [BlockerActivity] overlay.
     */
    private fun maybeBlock(pkg: String) {
        // Never block system-critical packages
        if (pkg in NEVER_BLOCK) return

        // Don't enforce when screen is off or device is locked
        if (!pm.isInteractive) return
        if (km?.isKeyguardLocked == true) return

        // Enforcement cadence: avoid duplicate blocks for the same package
        if (!shouldRunEnforcement(pkg)) return

        // Check if blocking is active for this package
        if (!ActiveBlockingState.shouldBlock(pkg)) return

        showBlockerOverlay(pkg)
    }

    /**
     * Rate-limit enforcement per package to avoid spamming the overlay
     * during event storms (many events can fire during a single app transition).
     */
    private fun shouldRunEnforcement(pkg: String): Boolean {
        val now = System.currentTimeMillis()
        val minGap = 150L  // At most one enforcement per 150ms per package
        val last = lastEnforceAtByPkg[pkg] ?: 0L
        if (now - last < minGap) return false
        lastEnforceAtByPkg[pkg] = now
        return true
    }

    /**
     * Show the blocker overlay instead of force-closing the app.
     * The overlay covers the blocked app with a "This app is blocked by
     * Tyme Boxed" message and a "Go Back" button that sends the user home.
     */
    private fun showBlockerOverlay(pkg: String) {
        val now = System.currentTimeMillis()

        // Don't show if blocker is already visible for this package
        if (BlockerActivity.isVisible && BlockerActivity.visiblePkg == pkg) return

        // Debounce overlay launches
        val lastShown = lastBlockShownAt[pkg] ?: 0L
        if ((now - lastShown) < BLOCK_SHOWN_COOLDOWN_MS) return
        if ((now - lastGlobalBlockTs) < 250L) return

        lastBlockShownAt[pkg] = now
        lastGlobalBlockTs = now

        // Resolve the app's display name
        val label = try {
            val ai = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(ai).toString()
        } catch (_: Throwable) {
            pkg
        }

        Log.d(TAG, "Showing blocker overlay for $pkg ($label)")

        // Launch the blocker overlay activity
        BlockerActivity.show(this, pkg, label)
    }

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    companion object {
        private const val TAG = "AppBlockerA11y"

        /**
         * Packages that must never be redirected, regardless of block list.
         * Blocking the launcher or system UI would soft-lock the device.
         * Expanded set covers common OEM launchers.
         */
        private val NEVER_BLOCK = setOf(
            "dev.ambitionsoftware.tymeboxed",       // our own app
            "com.android.systemui",                  // status bar, notifications
            "com.android.launcher3",                 // AOSP launcher
            "com.google.android.apps.nexuslauncher", // Pixel launcher
            "com.sec.android.app.launcher",          // Samsung launcher
            "com.samsung.android.app.routines",      // Samsung Routines
            "com.miui.home",                         // Xiaomi launcher
            "com.huawei.android.launcher",           // Huawei launcher
            "com.oppo.launcher",                     // Oppo launcher
            "com.realme.launcher",                   // Realme launcher
            "com.vivo.launcher",                     // Vivo launcher
            "com.oneplus.launcher",                  // OnePlus launcher
            "com.nothing.launcher",                  // Nothing launcher
            "com.teslacoilsw.launcher",              // Nova Launcher
            "com.microsoft.launcher",                // Microsoft Launcher
            "com.android.settings",                  // System settings
            "com.android.packageinstaller",          // Permission dialogs
            "com.google.android.permissioncontroller",
            "com.google.android.packageinstaller",
            "android",                               // System process
            // Accessibility settings — must never be blocked or user can't
            // toggle accessibility on/off
            "com.android.server.accessibility",
            "com.samsung.accessibility",
        )
    }
}
