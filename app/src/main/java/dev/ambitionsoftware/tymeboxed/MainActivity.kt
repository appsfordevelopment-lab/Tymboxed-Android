package dev.ambitionsoftware.tymeboxed

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.ambitionsoftware.tymeboxed.data.prefs.AppPreferences
import dev.ambitionsoftware.tymeboxed.ui.navigation.TymeBoxedNavHost
import dev.ambitionsoftware.tymeboxed.ui.theme.TbTheme
import javax.inject.Inject

/**
 * Single-activity host. Everything is Compose-based; the activity exists
 * only to own the NavHost, the theme wrapper, and NFC intent delivery.
 *
 * NFC is declared in the manifest with `ACTION_NDEF_DISCOVERED` /
 * `ACTION_TAG_DISCOVERED`, which means the OS will launch this activity
 * (or deliver to `onNewIntent` if already foreground) whenever a tag is
 * scanned while Tyme Boxed is in the foreground. Phase 4 wires the NFC
 * read into `StrategyCoordinator.handleNfcTag(tagId)`; Phase 1 just logs.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TbTheme {
                TymeBoxedNavHost(prefs = appPreferences)
            }
        }

        handleNfcIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        val action = intent?.action ?: return
        if (action != NfcAdapter.ACTION_NDEF_DISCOVERED &&
            action != NfcAdapter.ACTION_TAG_DISCOVERED &&
            action != NfcAdapter.ACTION_TECH_DISCOVERED
        ) return

        // Phase 1: just log the tag UID. Phase 4 routes this into the
        // strategy coordinator so scanning a tag starts / stops a session.
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        val uid = tag?.id?.joinToString(":") { "%02x".format(it) } ?: "unknown"
        Log.i(TAG, "NFC tag scanned (Phase 1 stub): uid=$uid, action=$action")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
