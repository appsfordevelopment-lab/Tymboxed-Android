package dev.ambitionsoftware.tymeboxed.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

object GoogleSignInHelper {

    fun client(activity: Activity, webClientId: String): GoogleSignInClient {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun idTokenFromResult(data: Intent?): Result<String> {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrBlank()) {
                Result.failure(Exception("Google did not return an ID token"))
            } else {
                Result.success(token)
            }
        } catch (e: ApiException) {
            // GoogleSignInStatusCodes.SIGN_IN_CANCELLED
            if (e.statusCode == 12501) {
                Result.failure(CancelledException())
            } else if (e.statusCode == CommonStatusCodes.DEVELOPER_ERROR) {
                Result.failure(
                    DeveloperConfigException(
                        "Google Sign-In is misconfigured (error 10). In Google Cloud Console " +
                            "(APIs & Services → Credentials), add an Android OAuth client whose " +
                            "package name matches this build (debug: dev.ambitionsoftware.tymeboxed.debug) " +
                            "and whose SHA-1 is from ./gradlew :app:signingReport. " +
                            "Keep using the Web client ID in GOOGLE_WEB_CLIENT_ID.",
                    ),
                )
            } else {
                Result.failure(Exception(e.message ?: "Google sign-in failed (${e.statusCode})"))
            }
        }
    }

    class CancelledException : Exception("Sign-in cancelled")

    class DeveloperConfigException(message: String) : Exception(message)
}
