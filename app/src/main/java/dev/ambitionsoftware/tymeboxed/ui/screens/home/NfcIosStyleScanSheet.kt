@file:OptIn(ExperimentalMaterial3Api::class)

package dev.ambitionsoftware.tymeboxed.ui.screens.home

import android.nfc.NfcAdapter
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ambitionsoftware.tymeboxed.nfc.normalizedUid

private const val LOG_TAG = "NfcIosScan"

/** iOS Core NFC–style palette for the scan sheet. */
private object NfcIosSheet {
    val sheetBackground = Color(0xFF2C2C2E)
    val title = Color.White
    val message = Color(0xFFD1D1D6)
    val systemBlue = Color(0xFF0A84FF)
    val error = Color(0xFFFF453A)
    val closeBg = Color.White.copy(alpha = 0.12f)
}

enum class NfcSessionScanPurpose {
    Start,
    Stop,
}

/**
 * System-style “Ready to Scan” sheet (iOS-like): dark panel, blue hero, full-width Cancel.
 * Invokes [onTagScanned] after any successful tag read (valid UID).
 */
@Composable
fun NfcIosStyleScanSheet(
    profileName: String,
    purpose: NfcSessionScanPurpose,
    onTagScanned: () -> Unit,
    onDismiss: () -> Unit,
) {
    val activity = LocalContext.current as ComponentActivity
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val triggerName = profileName.ifBlank { "session" }
    val bodyText = when (purpose) {
        NfcSessionScanPurpose.Start ->
            "Hold your phone near your Tyme Boxed device to trigger $triggerName."
        NfcSessionScanPurpose.Stop ->
            "Hold your phone near your Tyme Boxed device to end this session."
    }

    val latestOnSuccess by rememberUpdatedState(onTagScanned)
    val latestDismiss by rememberUpdatedState(onDismiss)
    var readerTornDown by remember { mutableStateOf(false) }
    var wrongTagMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (NfcAdapter.getDefaultAdapter(activity) == null) {
            Log.w(LOG_TAG, "No NFC adapter")
            latestDismiss()
        }
    }

    DisposableEffect(activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
            ?: return@DisposableEffect onDispose { }

        val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NFC_BARCODE

        adapter.enableReaderMode(
            activity,
            { tag ->
                val uid = tag.normalizedUid() ?: "unknown"
                Log.i(LOG_TAG, "Scanned uid=$uid")
                activity.runOnUiThread {
                    if (readerTornDown) return@runOnUiThread
                    wrongTagMessage = null
                    if (uid != "unknown") {
                        readerTornDown = true
                        runCatching { adapter.disableReaderMode(activity) }
                        latestOnSuccess()
                    } else {
                        wrongTagMessage = "Couldn’t read this tag. Try again."
                    }
                }
            },
            flags,
            null,
        )

        onDispose {
            if (!readerTornDown) {
                readerTornDown = true
                runCatching { adapter.disableReaderMode(activity) }
            }
        }
    }

    BackHandler(onBack = onDismiss)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NfcIosSheet.sheetBackground,
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(NfcIosSheet.message.copy(alpha = 0.35f)),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
            ) {
                Text(
                    text = "Ready to Scan",
                    color = NfcIosSheet.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NfcIosSheet.closeBg),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = NfcIosSheet.title,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = bodyText,
                color = NfcIosSheet.message,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            )

            wrongTagMessage?.let { msg ->
                Text(
                    text = msg,
                    color = NfcIosSheet.error,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            IosNfcScanningHero()

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NfcIosSheet.systemBlue,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun IosNfcScanningHero() {
    val transition = rememberInfiniteTransition(label = "nfcPulse")
    val outer by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "outer",
    )
    val mid by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mid",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp),
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .scale(outer)
                .border(2.dp, NfcIosSheet.systemBlue.copy(alpha = 0.35f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(102.dp)
                .scale(mid)
                .border(2.dp, NfcIosSheet.systemBlue.copy(alpha = 0.55f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(NfcIosSheet.systemBlue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}
