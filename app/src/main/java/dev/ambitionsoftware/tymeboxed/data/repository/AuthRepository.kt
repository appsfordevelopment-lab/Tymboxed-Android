package dev.ambitionsoftware.tymeboxed.data.repository

import com.google.gson.Gson
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

    suspend fun sendOtp(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        postAuth("/api/auth/send-otp", mapOf("email" to email))
    }

    suspend fun verifyOtp(email: String, otp: String): Result<Unit> = withContext(Dispatchers.IO) {
        postAuth("/api/auth/verify-otp", mapOf("email" to email, "otp" to otp))
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        postAuth("/api/auth/google", mapOf("idToken" to idToken))
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
