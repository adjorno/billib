package com.ifochka.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    fun start()

    suspend fun getIdToken(): String?

    suspend fun linkWithGoogle(): Result<Unit>

    suspend fun linkWithApple(): Result<Unit>
}
