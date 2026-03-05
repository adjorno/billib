package com.ifochka.m14n.data.auth

import com.ifochka.m14n.data.api.M14nApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
class WasmFirebaseAuthRepository(
    private val api: M14nApi,
) : AuthRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        scope.launch {
            val alreadySignedIn = runCatching { jsIsSignedIn() }.getOrDefault(false)
            if (!alreadySignedIn) {
                runCatching { jsSignInAnonymously().await<JsAny?>() }
                    .onFailure { println("[Auth] Anonymous sign-in failed: $it") }
            }
            _authState.value = if (!jsGetIsAnonymous()) AuthState.SignedIn else AuthState.Anonymous
            runCatching { api.syncUser() }
                .onFailure { println("[Auth] syncUser failed: $it") }
        }
    }

    override suspend fun getIdToken(): String? = runCatching { jsGetIdToken().await<JsAny?>()?.toString() }.getOrNull()

    // Implemented in Iteration 2
    override suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun linkWithGoogle(): Result<Unit> = Result.success(Unit)

    override suspend fun linkWithApple(): Result<Unit> = Result.success(Unit)
}
