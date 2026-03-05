package com.ifochka.m14n.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Firebase JS SDK interop for wasmJs is not yet implemented.
// wasmJs users appear as SignedIn with FIREBASE_API_KEY token fallback.
// Full implementation planned alongside Google/Apple sign-in (Iterations 4–5).
class WasmFirebaseAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.SignedIn)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun getIdToken(): String? = null

    override suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun linkWithGoogle(): Result<Unit> = Result.success(Unit)

    override suspend fun linkWithApple(): Result<Unit> = Result.success(Unit)
}
