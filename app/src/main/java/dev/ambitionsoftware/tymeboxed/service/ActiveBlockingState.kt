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
        val blockedPackages: Set<String> = emptySet(),
        val isAllowMode: Boolean = false,
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
        blockedPackages: Set<String>,
        isAllowMode: Boolean,
    ) {
        current = Snapshot(
            isBlocking = true,
            profileId = profileId,
            blockedPackages = blockedPackages,
            isAllowMode = isAllowMode,
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
}
