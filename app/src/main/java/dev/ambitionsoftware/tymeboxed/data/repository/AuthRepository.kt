package dev.ambitionsoftware.tymeboxed.data.repository

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import dev.ambitionsoftware.tymeboxed.BuildConfig
import dev.ambitionsoftware.tymeboxed.nfc.nfcTagIdLookupCandidates
import dev.ambitionsoftware.tymeboxed.nfc.NfcTagVerifyResult
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class AuthOtpEnvelope(
    val success: Boolean? = null,
    val message: String? = null,
    val expiresIn: Int? = null,
)

private data class NfcVerifyEnvelope(
    val success: Boolean? = null,
    val valid: Boolean? = null,
    val message: String? = null,
)

/**
 * Email OTP against [BuildConfig.TYMEBOXED_API_BASE_URL] (production: https://api.tymeboxed.app).
 */
@Singleton
class AuthRepository @Inject constructor() {
    private val gson = Gson()
    private val baseUrl = BuildConfig.TYMEBOXED_API_BASE_URL.trimEnd('/')

    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun sendOtp(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        postAuth("/api/auth/send-otp", mapOf("email" to email))
    }

    suspend fun verifyOtp(email: String, otp: String): Result<Unit> = withContext(Dispatchers.IO) {
        postAuth("/api/auth/verify-otp", mapOf("email" to email, "otp" to otp))
    }

    /**
     * Whether [tagId] is an active Tyme Boxed device tag in the API database
     * (`POST /api/nfc/verify`). Does not require login.
     * Tries several [nfcTagIdLookupCandidates] (colon vs. continuous hex, case, etc.) so the string
     * matches how the record was stored in MongoDB.
     */
    suspend fun verifyNfcTag(tagId: String): NfcTagVerifyResult = withContext(Dispatchers.IO) {
        val candidates = nfcTagIdLookupCandidates(tagId)
        if (candidates.isEmpty()) {
            return@withContext NfcTagVerifyResult.Error("Empty tag id")
        }
        var sawHttpError: String? = null
        var sawValidFalse = false
        for (candidate in candidates) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "NFC verify try tagId=\"$candidate\"")
            }
            val (code, text) = runCatching { postNfcVerifyHttp(candidate) }.getOrElse { e ->
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "NFC verify request failed for \"$candidate\": ${e.message}")
                }
                return@withContext NfcTagVerifyResult.Error(e.message)
            }
            if (code !in 200..299) {
                val msg = parseNfcErrorMessage(text) ?: text.ifBlank { "Request failed ($code)" }
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "NFC verify HTTP $code for \"$candidate\": $text")
                }
                // Same route for all candidates; don’t keep failing the user per variant
                return@withContext NfcTagVerifyResult.Error("HTTP $code: $msg")
            }
            val valid = parseNfcValidFlag(text)
            if (valid == true) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "NFC verify success with tagId=\"$candidate\"")
                }
                return@withContext NfcTagVerifyResult.Registered
            }
            if (valid == false) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "NFC verify valid=false for tagId=\"$candidate\"")
                }
                sawValidFalse = true
            } else {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "NFC verify could not parse 'valid' for tagId=\"$candidate\": $text")
                }
                sawHttpError = parseNfcErrorMessage(text) ?: "Unexpected response: ${text.take(200)}"
            }
        }
        when {
            sawValidFalse -> NfcTagVerifyResult.NotRegistered
            sawHttpError != null -> NfcTagVerifyResult.Error(sawHttpError)
            else -> NfcTagVerifyResult.Error("Could not verify this tag (no response from server).")
        }
    }

    private data class NfcHttpResult(val code: Int, val body: String)

    private fun postNfcVerifyHttp(tagId: String): NfcHttpResult {
        val jsonBody = gson.toJson(mapOf("tagId" to tagId))
        val url = URL(baseUrl + "/api/nfc/verify")
        var conn: HttpURLConnection? = null
        return try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 30_000
            }
            conn.outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()
            NfcHttpResult(code, text)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * `true` / `false` if the API clearly answered; [null] if JSON did not include a clear `valid` flag
     * (Gson and raw [JsonObject] for robustness).
     */
    private fun parseNfcValidFlag(responseBody: String): Boolean? {
        if (responseBody.isBlank()) return null
        val env = runCatching { gson.fromJson(responseBody, NfcVerifyEnvelope::class.java) }.getOrNull()
        when (val v = env?.valid) {
            true, false -> return v
            null -> Unit
        }
        val o = runCatching { JsonParser.parseString(responseBody).asJsonObject }.getOrNull() ?: return null
        val prim = when {
            o.has("valid") && !o.get("valid").isJsonNull -> o.get("valid")
            o.has("isValid") && !o.get("isValid").isJsonNull -> o.get("isValid")
            else -> return null
        }
        if (!prim.isJsonPrimitive) return null
        val p = prim.asJsonPrimitive
        return try {
            when {
                p.isBoolean -> p.asBoolean
                p.isString -> {
                    when (p.asString.trim().lowercase()) {
                        "true" -> true
                        "false" -> false
                        else -> null
                    }
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseNfcErrorMessage(responseBody: String): String? {
        if (responseBody.isBlank()) return null
        val o = runCatching { JsonParser.parseString(responseBody).asJsonObject }.getOrNull() ?: return null
        return o.get("message")?.asString
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) {
            decodeJwtAudience(idToken)?.let { aud ->
                Log.d(TAG, "Google ID token aud (must match API server config)=$aud")
            }
        }
        val result = postAuth("/api/auth/google", mapOf("idToken" to idToken))
        if (BuildConfig.DEBUG && result.isFailure) {
            val msg = result.exceptionOrNull()?.message.orEmpty()
            if (msg.contains("Invalid or expired Google token", ignoreCase = true)) {
                val aud = decodeJwtAudience(idToken)
                return@withContext Result.failure(
                    Exception(
                        "$msg\n\n(Debug) This JWT’s audience is:\n$aud\n\n" +
                            "api.tymeboxed.app only accepts tokens whose Web client ID matches its server " +
                            "configuration. Use that exact ID in GOOGLE_WEB_CLIENT_ID, or update the API env " +
                            "to verify this Web client ID.",
                    ),
                )
            }
        }
        result
    }

    /** JWT `aud` claim — the OAuth Web client ID used when the token was minted. */
    private fun decodeJwtAudience(idToken: String): String? {
        val parts = idToken.split('.')
        if (parts.size < 2) return null
        val json = try {
            val decoded = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            String(decoded, Charsets.UTF_8)
        } catch (_: Exception) {
            return null
        }
        return runCatching {
            JsonParser.parseString(json).asJsonObject.get("aud")?.asString
        }.getOrNull()
    }

    private fun postAuth(path: String, payload: Map<String, String>): Result<Unit> {
        val jsonBody = gson.toJson(payload)
        val url = URL(baseUrl + path)
        var conn: HttpURLConnection? = null
        return try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 30_000
            }
            conn.outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()
            val env = runCatching { gson.fromJson(text, AuthOtpEnvelope::class.java) }.getOrNull()
            when {
                code in 200..299 && env?.success == true -> Result.success(Unit)
                env?.message != null -> Result.failure(Exception(env.message))
                text.isNotBlank() -> Result.failure(Exception(text))
                else -> Result.failure(Exception("Request failed ($code)"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Could not reach server"))
        } finally {
            conn?.disconnect()
        }
    }
}
