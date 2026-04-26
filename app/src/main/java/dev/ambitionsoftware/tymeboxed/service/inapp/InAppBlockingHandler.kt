package dev.ambitionsoftware.tymeboxed.service.inapp

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import dev.ambitionsoftware.tymeboxed.service.BlockerActivity
import dev.ambitionsoftware.tymeboxed.R

/**
 * In-app surface blocking (YouTube, Instagram, X, Snapchat) — policy and a11y heuristics
 * are adapted from [Switchly public](https://gitlab.com/Saltyy/switchly-public): each
 * [InAppToggleKeys] is evaluated independently; detection/scoring for a surface only runs
 * when that toggle is enabled so e.g. “Block Shorts” only affects the Shorts surface.
 */
object InAppBlockingHandler {

    private const val TAG = "InAppBlocking"

    private const val SURFACE_BLOCK_COOLDOWN_MS = 800L
    private const val SURFACE_CONFIRM_MS = 450L
    private const val INAPP_POST_BLOCK_GRACE_MS = 650L
    private const val SURFACE_HINT_TTL_MS = 900L

    @JvmField
    @Volatile
    var currentSurfaceKey: String? = null

    @JvmField
    @Volatile
    var currentSurfacePkg: String? = null

    private val surfaceEvidenceCount = HashMap<String, Int>()
    private val surfaceEvidenceAt = HashMap<String, Long>()
    private val lastSurfaceBlockAt = HashMap<String, Long>()
    private val lastBlockShownAt = HashMap<String, Long>()
    private var lastGlobalBlockTs: Long = 0L
    private val inAppGraceUntilByPkg = HashMap<String, Long>()

    private val lastEnforceAtByPkg = HashMap<String, Long>()
    private val recentSurfaceHintKeyByPkg = HashMap<String, String>()
    private val recentSurfaceHintAtByPkg = HashMap<String, Long>()
    private val appEnteredAtByPkg = HashMap<String, Long>()

    private val mainHandler = Handler(Looper.getMainLooper())

    fun onAppTransition(pkg: String, now: Long) {
        appEnteredAtByPkg[pkg] = now
    }

    /**
     * @return `true` if an in-app block was applied (session / website block should not run for this pass).
     */
    fun maybeBlock(
        service: AccessibilityService,
        pkg: String,
        event: AccessibilityEvent?,
        root: AccessibilityNodeInfo,
    ): Boolean {
        if (!InAppToggleKeys.isSupportedApp(pkg)) return false
        if (!packageHasAnyBlockEnabled(service, pkg)) return false

        val now = System.currentTimeMillis()
        if (now < (inAppGraceUntilByPkg[pkg] ?: 0L)) return false
        // Single cadence: two consecutive shouldThrottleEnforce() calls on the same pass
        // would both update the same timestamp, so the second (150ms) always failed and
        // in-app blocking never reached the per-app handlers.

        val nowEvent = now
        if (nowEvent - (appEnteredAtByPkg[pkg] ?: 0L) > 30_000L) {
            appEnteredAtByPkg[pkg] = nowEvent
        }

        val eventType = event?.eventType ?: 0
        captureSurfaceHintFromEvent(pkg, event, now)

        val isTransition =
            eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED
        if (event != null && !isTransition) {
            if (shouldSkipLowSignalInApp(pkg, event, now)) return false
        }

        if (!shouldThrottleEnforce(pkg, now, 150L)) return false

        return when (pkg) {
            InAppToggleKeys.YOUTUBE -> handleYouTube(service, root, event, now)
            InAppToggleKeys.INSTAGRAM -> handleInstagram(service, root, event, now)
            InAppToggleKeys.X_TWITTER -> handleX(service, root, event, now)
            InAppToggleKeys.SNAPCHAT -> handleSnapchat(service, root, event, now)
            else -> false
        }
    }

    private fun shouldSkipLowSignalInApp(pkg: String, event: AccessibilityEvent, now: Long): Boolean {
        if (pkg == "com.google.android.youtube" && event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return (now - (appEnteredAtByPkg[pkg] ?: 0L)) in 0..<120
        }
        return false
    }

    private fun shouldThrottleEnforce(pkg: String, now: Long, gap: Long): Boolean {
        val last = lastEnforceAtByPkg[pkg] ?: 0L
        if (now - last < gap) return false
        lastEnforceAtByPkg[pkg] = now
        return true
    }

    private fun prefs(c: android.content.Context, key: String) =
        InAppBlockingPreferencesReader.isEnabled(c, key, false)

    private fun packageHasAnyBlockEnabled(c: android.content.Context, pkg: String): Boolean {
        return when (pkg) {
            InAppToggleKeys.YOUTUBE -> listOf(
                InAppToggleKeys.KEY_BLOCK_YT_SHORTS, InAppToggleKeys.KEY_BLOCK_YT_SEARCH,
                InAppToggleKeys.KEY_BLOCK_YT_COMMENTS, InAppToggleKeys.KEY_BLOCK_YT_PIP,
            ).any { prefs(c, it) }
            InAppToggleKeys.INSTAGRAM -> listOf(
                InAppToggleKeys.KEY_BLOCK_IG_REELS, InAppToggleKeys.KEY_BLOCK_IG_EXPLORE,
                InAppToggleKeys.KEY_BLOCK_IG_SEARCH, InAppToggleKeys.KEY_BLOCK_IG_STORIES,
                InAppToggleKeys.KEY_BLOCK_IG_COMMENTS,
            ).any { prefs(c, it) }
            InAppToggleKeys.X_TWITTER -> listOf(
                InAppToggleKeys.KEY_BLOCK_X_HOME, InAppToggleKeys.KEY_BLOCK_X_SEARCH,
                InAppToggleKeys.KEY_BLOCK_X_GROK, InAppToggleKeys.KEY_BLOCK_X_NOTIFICATIONS,
            ).any { prefs(c, it) }
            InAppToggleKeys.SNAPCHAT -> listOf(
                InAppToggleKeys.KEY_BLOCK_SNAP_MAP, InAppToggleKeys.KEY_BLOCK_SNAP_STORIES,
                InAppToggleKeys.KEY_BLOCK_SNAP_SPOTLIGHT, InAppToggleKeys.KEY_BLOCK_SNAP_FOLLOWING,
            ).any { prefs(c, it) }
            else -> false
        }
    }

    private fun rememberSurfaceHint(pkg: String, key: String, now: Long) {
        recentSurfaceHintKeyByPkg[pkg] = key
        recentSurfaceHintAtByPkg[pkg] = now
    }

    private fun recentSurfaceHintMatches(pkg: String, key: String, now: Long): Boolean {
        val k = recentSurfaceHintKeyByPkg[pkg] ?: return false
        val at = recentSurfaceHintAtByPkg[pkg] ?: return false
        return k == key && (now - at) <= SURFACE_HINT_TTL_MS
    }

    private fun clearSurfaceHint(pkg: String) {
        recentSurfaceHintKeyByPkg.remove(pkg)
        recentSurfaceHintAtByPkg.remove(pkg)
    }

    private fun clearSurfaceEvidence(vararg keys: String) {
        for (k in keys) {
            surfaceEvidenceCount.remove(k)
            surfaceEvidenceAt.remove(k)
        }
    }

    private fun surfaceConfirmed(key: String, detected: Boolean, required: Int = 2): Boolean {
        val now = System.currentTimeMillis()
        if (!detected) {
            clearSurfaceEvidence(key)
            return false
        }
        val lastAt = surfaceEvidenceAt[key] ?: 0L
        val count = if (now - lastAt <= SURFACE_CONFIRM_MS) (surfaceEvidenceCount[key] ?: 0) + 1 else 1
        surfaceEvidenceAt[key] = now
        surfaceEvidenceCount[key] = count
        return count >= required.coerceAtLeast(1)
    }

    private fun timedBlockMsg(
        c: android.content.Context,
        toggle: Boolean,
        label: String,
    ): Pair<String, String>? {
        if (!toggle) return null
        return c.getString(R.string.in_app_content_blocked_title, label) to
            c.getString(R.string.in_app_content_blocked_message, label)
    }

    private fun safeAppLabel(svc: AccessibilityService, pkg: String): String {
        return try {
            val ai = svc.applicationContext.packageManager.getApplicationInfo(pkg, 0)
            svc.applicationContext.packageManager.getApplicationLabel(ai).toString()
        } catch (_: Throwable) {
            pkg
        }
    }

    private fun softBlockSurface(
        service: AccessibilityService,
        pkg: String,
        appLabel: String,
        title: String,
        message: String,
        backCount: Int = 1,
        /**
         * When false, only the back key sequence runs.
         * No full-screen [BlockerActivity] (used for some non-YouTube cases).
         */
        showInAppBlocker: Boolean = true,
        /**
         * YouTube Shorts: jump to the YouTube home feed, then show the in-app overlay.
         * Dismiss is wired to return to YouTube home via [BlockerActivity.showInApp] extras.
         */
        openYouTubeHomeFirst: Boolean = false,
        dismissToYouTubeHome: Boolean = false,
    ) {
        val now = System.currentTimeMillis()
        val sk = "$pkg|$title"
        if (now - (lastSurfaceBlockAt[sk] ?: 0L) < SURFACE_BLOCK_COOLDOWN_MS) return
        if (BlockerActivity.isVisible) return
        if (now - (lastBlockShownAt[pkg] ?: 0L) < 800L || now - lastGlobalBlockTs < 250L) return

        lastSurfaceBlockAt[sk] = now
        lastBlockShownAt[pkg] = now
        lastGlobalBlockTs = now
        inAppGraceUntilByPkg[pkg] = now + INAPP_POST_BLOCK_GRACE_MS
        currentSurfaceKey = null
        currentSurfacePkg = null
        clearSurfaceHint(pkg)
        for (e in surfaceEvidenceAt.keys) {
            surfaceEvidenceAt.remove(e)
            surfaceEvidenceCount.remove(e)
        }

        val ctx = service.applicationContext
        if (openYouTubeHomeFirst && showInAppBlocker) {
            mainHandler.post {
                runCatching { BlockerActivity.openYouTubeHome(ctx) }
            }
            mainHandler.postDelayed(
                {
                    runCatching {
                        BlockerActivity.showInApp(
                            ctx, pkg, appLabel, title, message,
                            dismissToYouTubeHome = dismissToYouTubeHome,
                        )
                    }
                },
                200L,
            )
            return
        }

        if (backCount > 0) {
            var step = 0
            val runBack = object : Runnable {
                override fun run() {
                    if (step < backCount) {
                        runCatching { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }
                        step++
                        mainHandler.postDelayed(this, 120L)
                    } else {
                        if (showInAppBlocker) {
                            mainHandler.postDelayed({
                                runCatching {
                                    BlockerActivity.showInApp(
                                        ctx, pkg, appLabel, title, message,
                                        dismissToYouTubeHome = dismissToYouTubeHome,
                                    )
                                }
                            }, 40L)
                        }
                    }
                }
            }
            mainHandler.post(runBack)
        } else {
            if (showInAppBlocker) {
                mainHandler.postDelayed({
                    runCatching {
                        BlockerActivity.showInApp(
                            ctx, pkg, appLabel, title, message,
                            dismissToYouTubeHome = dismissToYouTubeHome,
                        )
                    }
                }, 30L)
            }
        }
    }

    // --- YouTube ---------------------------------------------------------------------------

    /**
     * True when the user is in the **Shorts feed** (Shorts bottom-nav tab is selected), not
     * when the word "Shorts" appears on Home/Subscriptions (that caused false blocks of the
     * whole app via [BlockerActivity]).
     */
    private fun isYouTubeShortsScreen(root: AccessibilityNodeInfo, event: AccessibilityEvent?): Boolean {
        if (InAppA11yNodes.hasSelectedOrCheckedLabel(root, listOf("shorts"))) return true
        // YouTube may expose the active tab with focus/selected events on some versions.
        val et = event?.eventType ?: 0
        if (et == AccessibilityEvent.TYPE_VIEW_SELECTED || et == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            if (InAppA11yNodes.eventTextMatches(event, listOf("shorts")) &&
                !InAppA11yNodes.eventTextMatches(event, listOf("search", "home", "subscriptions", "library"))
            ) {
                return true
            }
        }
        return false
    }

    private fun isYouTubeSearchScreen(root: AccessibilityNodeInfo, event: AccessibilityEvent?): Boolean {
        return InAppA11yNodes.nodeTextMatches(
            root,
            listOf("search youtube", "youtube durchsuchen", "search", "suche"),
        ) || InAppA11yNodes.eventTextMatches(event, listOf("search", "youtube", "suche"))
    }

    private fun isYouTubeCommentsVisible(root: AccessibilityNodeInfo): Boolean {
        return InAppA11yNodes.nodeTextMatches(
            root,
            listOf("comments", "kommentare", "add a comment", "sort comments"),
        )
    }

    private fun handleYouTube(
        service: AccessibilityService,
        root: AccessibilityNodeInfo,
        event: AccessibilityEvent?,
        now: Long,
    ): Boolean {
        val c = service.applicationContext
        val blockShorts = prefs(c, InAppToggleKeys.KEY_BLOCK_YT_SHORTS)
        // Only score / block Shorts when that toggle is on (Switchly-style: one toggle = one feature).
        if (blockShorts) {
            val eventType = event?.eventType ?: 0
            val ytQuickEvent =
                eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                    eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
                    eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
                    eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            val shortsNeed = if (ytQuickEvent) 1 else 2
            val shortsDetected = isYouTubeShortsScreen(root, event)
            val isShorts = surfaceConfirmed("yt:shorts", shortsDetected, required = shortsNeed)
            if (isShorts) {
                currentSurfaceKey = "yt:shorts"
                currentSurfacePkg = InAppToggleKeys.YOUTUBE
                val m = timedBlockMsg(
                    c,
                    true,
                    c.getString(R.string.in_app_label_shorts),
                )
                if (m != null) {
                    softBlockSurface(
                        service,
                        InAppToggleKeys.YOUTUBE,
                        safeAppLabel(service, InAppToggleKeys.YOUTUBE),
                        m.first,
                        m.second,
                        backCount = 0,
                        showInAppBlocker = true,
                        openYouTubeHomeFirst = true,
                        dismissToYouTubeHome = true,
                    )
                    return true
                }
            } else {
                if (currentSurfacePkg == InAppToggleKeys.YOUTUBE && currentSurfaceKey == "yt:shorts") {
                    currentSurfaceKey = null
                    currentSurfacePkg = null
                }
            }
        } else {
            clearSurfaceEvidence("yt:shorts")
            if (currentSurfacePkg == InAppToggleKeys.YOUTUBE && currentSurfaceKey == "yt:shorts") {
                currentSurfaceKey = null
                currentSurfacePkg = null
            }
        }

        if (prefs(c, InAppToggleKeys.KEY_BLOCK_YT_SEARCH) && isYouTubeSearchScreen(root, event)) {
            val m = timedBlockMsg(c, true, c.getString(R.string.in_app_label_search)) ?: return false
            softBlockSurface(service, InAppToggleKeys.YOUTUBE, safeAppLabel(service, InAppToggleKeys.YOUTUBE), m.first, m.second, backCount = 1)
            return true
        }
        if (prefs(c, InAppToggleKeys.KEY_BLOCK_YT_COMMENTS) && isYouTubeCommentsVisible(root)) {
            val m = timedBlockMsg(c, true, c.getString(R.string.in_app_label_comments)) ?: return false
            softBlockSurface(service, InAppToggleKeys.YOUTUBE, safeAppLabel(service, InAppToggleKeys.YOUTUBE), m.first, m.second, backCount = 1)
            return true
        }
        if (prefs(c, InAppToggleKeys.KEY_BLOCK_YT_PIP)) {
            val cls = event?.className?.toString().orEmpty()
            if (cls.contains("PictureInPicture", ignoreCase = true)) {
                val m = timedBlockMsg(c, true, c.getString(R.string.in_app_label_pip)) ?: return false
                softBlockSurface(service, InAppToggleKeys.YOUTUBE, safeAppLabel(service, InAppToggleKeys.YOUTUBE), m.first, m.second, backCount = 1)
                return true
            }
        }
        return false
    }

    private fun isInstagramSearchScreen(root: AccessibilityNodeInfo, event: AccessibilityEvent?): Boolean {
        return InAppA11yNodes.hasSelectedLabel(
            root,
            listOf("search", "suche", "discover", "entdecken", "explore"),
        ) || InAppA11yNodes.nodeTextMatches(
            root,
            listOf("search", "suche", "discover", "entdecken", "explore"),
        ) || InAppA11yNodes.eventTextMatches(
            event,
            listOf("search", "suche", "discover", "entdecken", "explore"),
        )
    }

    private fun isInstagramStoriesViewer(root: AccessibilityNodeInfo, event: AccessibilityEvent?): Boolean {
        return InAppA11yNodes.nodeTextMatches(
            root,
            listOf("send message", "nachricht senden", "reply", "antworten", "story"),
        ) || InAppA11yNodes.eventTextMatches(event, listOf("story", "stories"))
    }

    private fun isInstagramCommentsVisible(root: AccessibilityNodeInfo): Boolean {
        return InAppA11yNodes.nodeTextMatches(
            root,
            listOf("comments", "kommentare", "add a comment", "view all comments"),
        )
    }

    private fun instagramState(root: AccessibilityNodeInfo, event: AccessibilityEvent?): String? {
        return when {
            isInstagramStoriesViewer(root, event) -> "stories"
            InAppA11yNodes.hasSelectedLabel(root, listOf("reels")) || InAppA11yNodes.eventTextMatches(
                event,
                listOf("reels", "watch more reels", "send reel"),
            ) -> "reels"
            isInstagramSearchScreen(root, event) -> "explore"
            InAppA11yNodes.hasSelectedLabel(root, listOf("home", "startseite")) -> "home"
            else -> null
        }
    }

    private fun handleInstagram(
        service: AccessibilityService,
        root: AccessibilityNodeInfo,
        event: AccessibilityEvent?,
        now: Long,
    ): Boolean {
        val c = service.applicationContext
        val eventType = event?.eventType ?: 0
        val blockReels = prefs(c, InAppToggleKeys.KEY_BLOCK_IG_REELS)
        val blockExplore = prefs(c, InAppToggleKeys.KEY_BLOCK_IG_EXPLORE)
        val blockSearch = prefs(c, InAppToggleKeys.KEY_BLOCK_IG_SEARCH)
        val blockStories = prefs(c, InAppToggleKeys.KEY_BLOCK_IG_STORIES)
        val blockComments = prefs(c, InAppToggleKeys.KEY_BLOCK_IG_COMMENTS)

        val reelsTab = InAppA11yNodes.hasSelectedLabel(root, listOf("reels"))
        val exploreTab = InAppA11yNodes.hasSelectedLabel(root, listOf("search", "suche", "discover", "entdecken", "explore"))
        val searchNow = isInstagramSearchScreen(root, event)
        val storiesNow = isInstagramStoriesViewer(root, event)
        val stateById = instagramState(root, event)

        if (storiesNow && blockStories) {
            currentSurfaceKey = "ig:stories"
            currentSurfacePkg = InAppToggleKeys.INSTAGRAM
            val m = timedBlockMsg(c, true, c.getString(R.string.in_app_label_stories))
            if (m != null) {
                softBlockSurface(
                    service,
                    InAppToggleKeys.INSTAGRAM,
                    safeAppLabel(service, InAppToggleKeys.INSTAGRAM),
                    m.first,
                    m.second,
                    backCount = 1,
                )
                return true
            }
        }

        val reelsRequired = if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED || eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) 1 else 2
        val reelsHit = surfaceConfirmed(
            "ig:reels",
            blockReels && stateById == "reels" && reelsTab,
            required = reelsRequired,
        )
        if (reelsHit) {
            currentSurfaceKey = "ig:reels"
            currentSurfacePkg = InAppToggleKeys.INSTAGRAM
            val m = timedBlockMsg(c, true, c.getString(R.string.in_app_label_reels)) ?: return false
            softBlockSurface(service, InAppToggleKeys.INSTAGRAM, safeAppLabel(service, InAppToggleKeys.INSTAGRAM), m.first, m.second, backCount = 2)
            return true
        }

        val exploreHit = surfaceConfirmed(
            "ig:explore",
            blockExplore && exploreTab && !searchNow && (stateById == "explore" || exploreTab),
            required = 1,
        )
        if (exploreHit) {
            currentSurfaceKey = "ig:explore"
            currentSurfacePkg = InAppToggleKeys.INSTAGRAM
            val m = timedBlockMsg(c, true, c.getString(R.string.in_app_label_explore_tab)) ?: return false
            softBlockSurface(service, InAppToggleKeys.INSTAGRAM, safeAppLabel(service, InAppToggleKeys.INSTAGRAM), m.first, m.second, backCount = 2)
            return true
        }

        val searchHit = surfaceConfirmed(
            "ig:search",
            blockSearch && searchNow,
            required = 1,
        )
        if (searchHit) {
            currentSurfaceKey = "ig:search"
            currentSurfacePkg = InAppToggleKeys.INSTAGRAM
            val m = timedBlockMsg(c, true, c.getString(R.string.in_app_label_search)) ?: return false
            softBlockSurface(service, InAppToggleKeys.INSTAGRAM, safeAppLabel(service, InAppToggleKeys.INSTAGRAM), m.first, m.second, backCount = 2)
            return true
        }

        if (blockComments && isInstagramCommentsVisible(root)) {
            val m = timedBlockMsg(c, true, c.getString(R.string.in_app_label_comments)) ?: return false
            softBlockSurface(service, InAppToggleKeys.INSTAGRAM, safeAppLabel(service, InAppToggleKeys.INSTAGRAM), m.first, m.second, backCount = 1)
            return true
        }
        if (!reelsHit && !exploreHit && !searchHit && !storiesNow) {
            if (currentSurfacePkg == InAppToggleKeys.INSTAGRAM) {
                currentSurfaceKey = null
                currentSurfacePkg = null
            }
        }
        return false
    }

    // --- X / Twitter ----------------------------------------------------------------------

    private fun handleX(
        service: AccessibilityService,
        root: AccessibilityNodeInfo,
        event: AccessibilityEvent?,
        now: Long,
    ): Boolean {
        val c = service.applicationContext
        val p = InAppToggleKeys.X_TWITTER
        val homeN = listOf("home", "home timeline", "startseite", "for you", "following")
        if (handleXOne(
                service, root, event, p, "x:foryou",
                prefs(c, InAppToggleKeys.KEY_BLOCK_X_HOME), homeN, c.getString(R.string.in_app_label_home), now
            )
        ) return true
        if (handleXOne(
                service, root, event, p, "x:search",
                prefs(c, InAppToggleKeys.KEY_BLOCK_X_SEARCH), listOf("search", "explore"), c.getString(R.string.in_app_label_search), now
            )
        ) return true
        if (handleXOne(
                service, root, event, p, "x:grok",
                prefs(c, InAppToggleKeys.KEY_BLOCK_X_GROK), listOf("grok"), c.getString(R.string.in_app_label_grok), now
            )
        ) return true
        if (handleXOne(
                service, root, event, p, "x:notifications",
                prefs(c, InAppToggleKeys.KEY_BLOCK_X_NOTIFICATIONS),
                listOf("notifications", "notification"), c.getString(R.string.in_app_label_notifications), now
            )
        ) return true
        if (currentSurfacePkg == p) {
            currentSurfaceKey = null
            currentSurfacePkg = null
        }
        return false
    }

    private fun handleXOne(
        service: AccessibilityService,
        root: AccessibilityNodeInfo,
        event: AccessibilityEvent?,
        pkg: String,
        surfaceKey: String,
        block: Boolean,
        needles: List<String>,
        label: String,
        now: Long,
    ): Boolean {
        if (!block) {
            clearSurfaceEvidence(surfaceKey)
            return false
        }
        val detected =
            recentSurfaceHintMatches(pkg, surfaceKey, now) ||
                InAppA11yNodes.hasSelectedLabelInPackage(root, needles, pkg) ||
                InAppA11yNodes.eventTextMatches(event, needles)
        val need = if (recentSurfaceHintMatches(pkg, surfaceKey, now) || InAppA11yNodes.eventTextMatches(event, needles)) 1 else 2
        val hit = surfaceConfirmed(surfaceKey, detected, required = need)
        if (!hit) return false
        currentSurfaceKey = surfaceKey
        currentSurfacePkg = pkg
        val m = timedBlockMsg(service.applicationContext, true, label) ?: return false
        softBlockSurface(service, pkg, safeAppLabel(service, pkg), m.first, m.second, backCount = 0)
        return true
    }

    // --- Snapchat -------------------------------------------------------------------------

    private fun handleSnapchat(
        service: AccessibilityService,
        root: AccessibilityNodeInfo,
        event: AccessibilityEvent?,
        now: Long,
    ): Boolean {
        val c = service.applicationContext
        val p = InAppToggleKeys.SNAPCHAT
        if (doSnap(
                service, root, "snap:map", prefs(c, InAppToggleKeys.KEY_BLOCK_SNAP_MAP),
                listOf("map", "snap map"), c.getString(R.string.in_app_label_map), p, now
            )
        ) return true
        if (doSnap(
                service, root, "snap:stories", prefs(c, InAppToggleKeys.KEY_BLOCK_SNAP_STORIES),
                listOf("stories", "story"), c.getString(R.string.in_app_label_stories), p, now
            )
        ) return true
        if (doSnap(
                service, root, "snap:spotlight", prefs(c, InAppToggleKeys.KEY_BLOCK_SNAP_SPOTLIGHT),
                listOf("spotlight"), c.getString(R.string.in_app_label_spotlight), p, now
            )
        ) return true
        if (doSnap(
                service, root, "snap:following", prefs(c, InAppToggleKeys.KEY_BLOCK_SNAP_FOLLOWING),
                listOf("following"), c.getString(R.string.in_app_label_following), p, now
            )
        ) return true
        if (currentSurfacePkg == p) {
            currentSurfaceKey = null
            currentSurfacePkg = null
        }
        return false
    }

    private fun doSnap(
        service: AccessibilityService,
        root: AccessibilityNodeInfo,
        surfaceKey: String,
        block: Boolean,
        needles: List<String>,
        label: String,
        pkg: String,
        @Suppress("unused") now: Long,
    ): Boolean {
        if (!block) return false
        val det = InAppA11yNodes.hasSelectedLabelInPackage(root, needles, pkg) ||
            InAppA11yNodes.nodeTextMatches(root, needles)
        if (!det) return false
        val hit = surfaceConfirmed(surfaceKey, true, 1)
        if (!hit) return false
        currentSurfaceKey = surfaceKey
        currentSurfacePkg = pkg
        val m = timedBlockMsg(service.applicationContext, true, label) ?: return false
        Log.d(TAG, "Snap block surface=$surfaceKey")
        softBlockSurface(service, pkg, safeAppLabel(service, pkg), m.first, m.second, backCount = 0)
        return true
    }

    private fun captureSurfaceHintFromEvent(pkg: String, event: AccessibilityEvent?, now: Long) {
        if (event == null) return
        val type = event.eventType
        if (type != AccessibilityEvent.TYPE_VIEW_CLICKED &&
            type != AccessibilityEvent.TYPE_VIEW_SELECTED &&
            type != AccessibilityEvent.TYPE_VIEW_FOCUSED
        ) {
            return
        }
        when (pkg) {
            InAppToggleKeys.X_TWITTER -> {
                when {
                    InAppA11yNodes.eventTextMatches(event, listOf("home", "home timeline", "for you", "following", "startseite")) -> rememberSurfaceHint(
                        pkg, "x:foryou", now
                    )
                    InAppA11yNodes.eventTextMatches(event, listOf("search", "explore")) -> rememberSurfaceHint(
                        pkg, "x:search", now
                    )
                    InAppA11yNodes.eventTextMatches(event, listOf("grok")) -> rememberSurfaceHint(
                        pkg, "x:grok", now
                    )
                    InAppA11yNodes.eventTextMatches(event, listOf("notifications", "notification")) -> rememberSurfaceHint(
                        pkg, "x:notifications", now
                    )
                }
            }
        }
    }
}
