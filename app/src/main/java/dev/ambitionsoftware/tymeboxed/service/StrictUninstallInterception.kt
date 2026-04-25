package dev.ambitionsoftware.tymeboxed.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Locale

/**
 * Android counterpart to iOS [ManagedSettingsStore.application.denyAppRemoval] in
 * [AppBlockerUtil.swift] — the OS does not offer a public third-party "deny uninstall" API,
 * so we scan Settings / package installers / Play for UI that references Tyme Boxed.
 */
object StrictUninstallInterception {

    private val main by lazy { Handler(Looper.getMainLooper()) }
    @Volatile
    private var lastInterceptAt: Long = 0L
    private const val COOLDOWN_MS = 1_200L

    private val WATCHED_PACKAGES: Set<String> = setOf(
        "com.android.settings",
        "com.android.settings.intelligence",
        "com.google.android.settings",
        "com.google.android.apps.wellbeing",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.samsung.android.packageinstaller",
        "com.samsung.android.settings",
        "com.samsung.android.app.settings",
        "com.miui.global.packageinstaller",
        "com.miui.packageinstaller",
        "com.miui.securitycenter",
        "com.android.vending",
        "com.coloros.safecenter",
        "com.oplus.safecenter",
        "com.oplus.appdetail",
        "com.oneplus.security",
        "com.huawei.systemmanager",
        "com.hihonor.devicemanager",
    )

    fun interceptIfNeeded(
        service: AccessibilityService,
        foregroundPackage: String,
    ): Boolean {
        val appContext: Context = service.applicationContext
        val myPkg = appContext.packageName
        val snap = ActiveBlockingState.current
        if (!snap.isBlocking || !snap.strictModeEnabled) return false
        if (foregroundPackage == myPkg) return false
        if (!isWatchedOrInstallerLike(foregroundPackage)) return false

        val combinedText = collectAllVisibleText(service) ?: return false
        if (!textReferencesTymeBoxed(appContext, combinedText)) return false

        val now = System.currentTimeMillis()
        if (now - lastInterceptAt < COOLDOWN_MS) return true
        lastInterceptAt = now

        return runCatching {
            runCatching { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }
            main.postDelayed(
                {
                    BlockerActivity.show(
                        context = appContext,
                        pkg = foregroundPackage,
                        label = "System",
                        headline = "Uninstall blocked",
                        body = "Strict mode is on. You can’t remove Tyme Boxed while a focus session " +
                            "is active. End the session in Tyme Boxed first.",
                    )
                },
                40L,
            )
            true
        }.getOrDefault(false)
    }

    private fun isWatchedOrInstallerLike(pkg: String): Boolean {
        if (pkg in WATCHED_PACKAGES) return true
        val p = pkg.lowercase(Locale.ROOT)
        if (p.contains("packageinstaller")) return true
        if (p.contains("vending") && p.contains("android")) return true
        if (p.endsWith(".settings") && (
                p.startsWith("com.google.") || p.startsWith("com.samsung.") ||
                    p.startsWith("com.miui.") || p.startsWith("com.oplus.") ||
                    p.startsWith("com.android.") || p.startsWith("com.oneplus.") ||
                    p.startsWith("com.hihonor.") || p.startsWith("com.huawei")
                )
        ) {
            return true
        }
        return false
    }

    private fun textReferencesTymeBoxed(context: Context, combined: String): Boolean {
        val flat = combined.lowercase(Locale.ROOT)
        if (flat.contains("tyme boxed") || flat.contains("tymeboxed") || flat.contains("tyme-boxed")) {
            return true
        }
        val myPkg = context.packageName.lowercase(Locale.ROOT)
        if (myPkg in flat) return true
        if (flat.contains("ambitionsoftware") && flat.contains("tyme")) return true
        val label = runCatching {
            context.applicationInfo.loadLabel(context.packageManager).toString().lowercase(Locale.ROOT)
        }.getOrNull().orEmpty()
        return label.isNotBlank() && label in flat
    }

    private fun collectAllVisibleText(service: AccessibilityService): String? {
        val myPkg = service.applicationContext.packageName
        return try {
            val sb = StringBuilder(1500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val list = runCatching { service.windows }.getOrNull()
                if (!list.isNullOrEmpty()) {
                    for (win in list) {
                        val r = win.root ?: continue
                        try {
                            if (r.packageName?.toString() == myPkg) {
                                // Skip [BlockerActivity] full-screen layers so "Tyme Boxed" in the
                                // overlay does not false-trigger while the user is in Settings.
                                continue
                            }
                            sb.append(' ').append(flattenNodeTreeToString(r))
                        } finally {
                            runCatching { r.recycle() }
                        }
                    }
                }
            }
            if (sb.isBlank()) {
                val root = service.rootInActiveWindow ?: return null
                if (root.packageName?.toString() == myPkg) {
                    runCatching { root.recycle() }
                    return null
                }
                try {
                    sb.append(' ').append(flattenNodeTreeToString(root))
                } finally {
                    runCatching { root.recycle() }
                }
            }
            sb.toString()
        } catch (_: Throwable) {
            null
        }
    }

    /** DFS; does not recycle [node]. */
    private fun flattenNodeTreeToString(node: AccessibilityNodeInfo): String {
        val out = StringBuilder(512)
        fun walk(n: AccessibilityNodeInfo) {
            n.text?.let { out.append(' ').append(it) }
            n.contentDescription?.let { out.append(' ').append(it) }
            repeat(n.childCount) { i ->
                val c = n.getChild(i) ?: return@repeat
                try {
                    walk(c)
                } finally {
                    runCatching { c.recycle() }
                }
            }
        }
        walk(node)
        return out.toString()
    }
}
