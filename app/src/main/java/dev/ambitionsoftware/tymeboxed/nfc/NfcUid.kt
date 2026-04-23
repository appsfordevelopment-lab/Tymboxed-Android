package dev.ambitionsoftware.tymeboxed.nfc

import android.nfc.Tag
import java.util.Locale

fun normalizeNfcUid(bytes: ByteArray?): String? {
    if (bytes == null || bytes.isEmpty()) return null
    return bytes.joinToString(":") { b -> "%02x".format(b) }.lowercase(Locale.ROOT)
}

fun normalizeNfcUid(raw: String): String = raw.trim().lowercase(Locale.ROOT)

fun Tag.normalizedUid(): String? = normalizeNfcUid(id)
