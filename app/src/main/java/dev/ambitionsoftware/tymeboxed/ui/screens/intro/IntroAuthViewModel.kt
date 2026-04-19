package dev.ambitionsoftware.tymeboxed.ui.screens.intro

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ambitionsoftware.tymeboxed.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class IntroAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    suspend fun requestOtp(email: String): Result<Unit> = authRepository.sendOtp(email.trim())

    suspend fun confirmOtp(email: String, code: String): Result<Unit> =
        authRepository.verifyOtp(email.trim(), code.trim())

    suspend fun signInWithGoogle(idToken: String): Result<Unit> =
        authRepository.signInWithGoogle(idToken)
}
