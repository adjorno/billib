package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun getIdToken(): String?

    suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit>

    suspend fun linkWithCredential(credential: AuthCredential): Result<Unit>
}
