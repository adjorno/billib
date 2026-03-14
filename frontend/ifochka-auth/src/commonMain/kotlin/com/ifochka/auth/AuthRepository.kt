package com.ifochka.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun getIdToken(): String?

    suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit>

    suspend fun linkWithGoogle(): Result<Unit>

    suspend fun linkWithApple(): Result<Unit>
}
