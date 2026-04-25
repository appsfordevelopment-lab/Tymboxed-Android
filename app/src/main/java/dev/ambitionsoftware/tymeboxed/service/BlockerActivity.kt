package dev.ambitionsoftware.tymeboxed.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Full-screen overlay shown when the user tries to open a blocked app.
 *
 * Modeled after Switchly's BlockerActivity — launched as a separate task
 * (excluded from recents) that covers the blocked app. The user can only
 * dismiss it by tapping "Go Back", which sends them to the home screen.
 *
 * Key design decisions:
 *  - Uses `FLAG_ACTIVITY_NEW_TASK` + `FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS`
 *    so it doesn't pollute the app's own task stack.
 *  - `taskAffinity=""` ensures it runs in its own task.
 *  - `setShowWhenLocked(true)` so it also shows on the lock screen if needed.
 *  - `FLAG_SECURE` prevents screenshots/screen recording of the overlay.
 *  - `isVisible` / `visiblePkg` statics prevent duplicate launches.
 *  - Uses a standalone Material 3 theme (no Hilt dependency) so it can be
 *    launched from the accessibility service without @AndroidEntryPoint.
 */
class BlockerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Privacy: don't show content in recents
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // Show on lock screen
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val pkg = intent?.getStringExtra(EXTRA_PKG).orEmpty()
        val label = intent?.getStringExtra(EXTRA_LABEL).orEmpty()
        val headline = intent?.getStringExtra(EXTRA_HEADLINE)
        val body = intent?.getStringExtra(EXTRA_BODY)

        setContent {
            BlockerTheme {
                BlockerScreen(
                    headline = headline ?: "App Blocked",
                    appName = label.ifBlank { pkg },
                    message = body ?: "This app is blocked by Tyme Boxed during your focus session. Stay focused!",
                    onGoBack = { handleClose() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        isVisible = true
        visiblePkg = intent?.getStringExtra(EXTRA_PKG)
    }

    override fun onPause() {
        isVisible = false
        visiblePkg = null
        super.onPause()
    }

    override fun onDestroy() {
        isVisible = false
        visiblePkg = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Back button sends user home (not back to the blocked app)
        handleClose()
    }

    private fun handleClose() {
        sendHome(this)
        finish()
    }

    companion object {
        private const val EXTRA_PKG = "pkg"
        private const val EXTRA_LABEL = "label"
        private const val EXTRA_HEADLINE = "headline"
        private const val EXTRA_BODY = "body"

        /** True while the blocker overlay is visible — prevents duplicate launches. */
        @Volatile
        var isVisible: Boolean = false
            private set

        @Volatile
        var visiblePkg: String? = null
            private set

        /**
         * Show the blocker overlay for the given package.
         * Called from [AppBlockerAccessibilityService] when a blocked app is detected.
         */
        fun show(
            context: Context,
            pkg: String,
            label: String?,
            headline: String? = null,
            body: String? = null,
        ) {
            val i = Intent(context, BlockerActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
                putExtra(EXTRA_PKG, pkg)
                if (!label.isNullOrBlank()) putExtra(EXTRA_LABEL, label)
                if (!headline.isNullOrBlank()) putExtra(EXTRA_HEADLINE, headline)
                if (!body.isNullOrBlank()) putExtra(EXTRA_BODY, body)
            }
            context.startActivity(i)
        }

        /** Overlay when a specific website is blocked inside a browser. */
        fun showForWebsite(context: Context, browserPkg: String, browserLabel: String, host: String) {
            show(
                context = context,
                pkg = browserPkg,
                label = host,
                headline = "Website blocked",
                body = "This website is blocked by Tyme Boxed during your focus session.\n($browserLabel)",
            )
        }

        /** In-app section blocking (e.g. Shorts, Reels) from Settings, independent of focus session. */
        fun showInApp(
            context: Context,
            pkg: String,
            appLabel: String,
            title: String,
            message: String,
        ) {
            show(
                context = context,
                pkg = pkg,
                label = appLabel,
                headline = title,
                body = message,
            )
        }

        /** Send user to the home screen. */
        fun sendHome(context: Context) {
            val home = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(home)
        }
    }
}

@Composable
private fun BlockerSessionElapsedLabel(sessionStartMs: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(sessionStartMs) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }
    val elapsedSec = ((now - sessionStartMs).coerceAtLeast(0L)) / 1000L
    val h = (elapsedSec / 3600).toInt()
    val m = ((elapsedSec % 3600) / 60).toInt()
    val s = (elapsedSec % 60).toInt()
    val label = String.format("Session blocking: %02d:%02d:%02d", h, m, s)
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}

// ---------------------------------------------------------------------------
// Standalone theme — no Hilt dependency so it works from the a11y service
// ---------------------------------------------------------------------------

/**
 * Accent color used across the blocker UI. Matches the "Warm Sandstone"
 * default from [dev.ambitionsoftware.tymeboxed.ui.theme.AccentColors].
 */
private val BlockerAccent = Color(0xFFC4A77D)

private val BlockerDarkScheme = darkColorScheme(
    primary = BlockerAccent,
    onPrimary = Color.White,
    background = Color(0xFF1C1C1E),
    surface = Color(0xFF2C2C2E),
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFFF5966),
    errorContainer = Color(0xFF93000A),
    onSurfaceVariant = Color(0xFFCAC4D0),
)

private val BlockerLightScheme = lightColorScheme(
    primary = BlockerAccent,
    onPrimary = Color.White,
    background = Color(0xFFF2F2F7),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color(0xFFFF5966),
    errorContainer = Color(0xFFFFDAD6),
    onSurfaceVariant = Color(0xFF49454F),
)

@Composable
private fun BlockerTheme(
    content: @Composable () -> Unit,
) {
    val scheme = if (isSystemInDarkTheme()) BlockerDarkScheme else BlockerLightScheme
    MaterialTheme(colorScheme = scheme, content = content)
}

// ---------------------------------------------------------------------------
// Compose UI
// ---------------------------------------------------------------------------

@Composable
private fun BlockerScreen(
    headline: String,
    appName: String,
    message: String,
    onGoBack: () -> Unit,
) {
    // Subtle pulsing animation on the icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.background,
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Pulsing icon with glow ring
                Box(contentAlignment = Alignment.Center) {
                    // Glow ring
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale)
                            .alpha(pulseAlpha)
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                shape = CircleShape,
                            )
                    )
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Blocked",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // App name
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )

                val sessionStart = ActiveBlockingState.current.sessionStartTimeMs
                if (sessionStart > 0L) {
                    Spacer(modifier = Modifier.height(10.dp))
                    BlockerSessionElapsedLabel(sessionStartMs = sessionStart)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Go Back button
                Button(
                    onClick = onGoBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = "Go Back",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
