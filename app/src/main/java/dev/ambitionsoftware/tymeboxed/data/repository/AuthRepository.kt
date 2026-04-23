package dev.ambitionsoftware.tymeboxed.data.repository

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import dev.ambitionsoftware.tymeboxed.BuildConfig
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
