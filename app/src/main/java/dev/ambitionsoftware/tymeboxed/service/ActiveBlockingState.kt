package dev.ambitionsoftware.tymeboxed.service

import android.os.SystemClock

/**
 * In-memory singleton that holds the current blocking session's state.
 *
 * Written by [HomeViewModel] when a session starts/stops. Read by
 * [AppBlockerAccessibilityService] on every window-change event — must be
 * fast (no DB, no suspension). Thread-safe via a single volatile reference
 * to an immutable snapshot.
 */
object ActiveBlockingState {

    data class Snapshot(
        val isBlocking: Boolean = false,
        val profileId: String? = null,
        /** Display name for the foreground notification; survives service restarts with a null intent. */
        val profileName: String? = null,
        val blockedPackages: Set<String> = emptySet(),
        val isAllowMode: Boolean = false,
        /** Normalized hostnames from the active profile (see [DomainBlocking.normalize]). */
        val domains: Set<String> = emptySet(),
        val isAllowModeDomains: Boolean = false,
    )

    @Volatile
    var current: Snapshot = Snapshot()
        private set

    /**
     * Heartbeat tracking for the accessibility service.
     * Updated every ~1 second by the service's tick loop.
     * The UI can check [isServiceAlive] to know if blocking is actually enforced.
     */
    @Volatile
    private var lastHeartbeatElapsed: Long = 0L

    /** How stale a heartbeat can be before we consider the service dead. */
    private const val HEARTBEAT_STALE_MS = 8_000L

    /**
     * Activate blocking for the given profile. Called when a session starts.
     */
    fun activate(
        profileId: String,
        profileName: String,
        blockedPackages: Set<String>,
        isAllowMode: Boolean,
        domains: List<String> = emptyList(),
        isAllowModeDomains: Boolean = false,
    ) {
        val normalizedDomains = domains
            .map { it.trim() }
            .mapNotNull { DomainBlocking.normalize(it) }
            .toSet()
        current = Snapshot(
            isBlocking = true,
            profileId = profileId,
            profileName = profileName,
            blockedPackages = blockedPackages,
            isAllowMode = isAllowMode,
            domains = normalizedDomains,
            isAllowModeDomains = isAllowModeDomains,
        )
    }

    /** Clear blocking state. Called when a session ends. */
    fun deactivate() {
        current = Snapshot()
    }

    /**
     * Called by [AppBlockerAccessibilityService] on every tick to signal
     * that the service is alive and actively enforcing blocks.
     */
    fun markHeartbeat() {
        lastHeartbeatElapsed = SystemClock.elapsedRealtime()
    }

    /** Mark the service as disconnected (called from onDestroy). */
    fun markDisconnected() {
        lastHeartbeatElapsed = 0L
    }

    /**
     * Returns true if the accessibility service is alive and actively enforcing.
     * Uses elapsed realtime (survives deep sleep) to detect stale heartbeats.
     */
    val isServiceAlive: Boolean
        get() {
            val last = lastHeartbeatElapsed
            if (last <= 0L) return false
            val age = SystemClock.elapsedRealtime() - last
            return age in 0..HEARTBEAT_STALE_MS
        }

    /**
     * Returns true if the given [packageName] should be blocked right now.
     *
     * - Allow mode: block everything **except** packages in the list.
     * - Block mode: block only packages **in** the list.
     *
     * Always returns false when blocking is inactive.
     */
    fun shouldBlock(packageName: String): Boolean {
        val snap = current
        if (!snap.isBlocking) return false
        return if (snap.isAllowMode) {
            packageName !in snap.blockedPackages
        } else {
            packageName in snap.blockedPackages
        }
    }

    /** True when the profile has at least one domain rule to enforce in browsers. */
    fun hasDomainRules(): Boolean = current.isBlocking && current.domains.isNotEmpty()

    /**
     * Whether the given [host] (from a browser URL bar) should be blocked.
     *
     * - Block mode: block when [host] matches any listed domain (including subdomains).
     * - Allow mode: block when [host] does **not** match any listed domain.
     */
    fun shouldBlockDomain(host: String): Boolean {
        val snap = current
        if (!snap.isBlocking || snap.domains.isEmpty()) return false
        val normalizedHost = DomainBlocking.normalize(host) ?: return false
        val matchesRule = snap.domains.any { DomainBlocking.matches(normalizedHost, it) }
        return if (snap.isAllowModeDomains) {
            !matchesRule
        } else {
            matchesRule
        }
    }
}
