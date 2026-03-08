package com.ifochka.m14n.data.auth

import com.ifochka.m14n.data.api.M14nApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
class WasmFirebaseAuthRepository(
    private val api: M14nApi,
    private val scope: CoroutineScope,
) : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        scope.launch {
            runCatching { jsGetRedirectResult().await<JsAny?>() }
                .onFailure { jsConsoleError("[Auth] getRedirectResult failed: $it") }
        }
        jsOnAuthStateChanged { isSignedIn ->
            _authState.value = if (isSignedIn) AuthState.SignedIn else AuthState.Anonymous
            scope.launch {
                runCatching { api.syncUser() }
                    .onFailure { jsConsoleError("[Auth] syncUser failed: $it") }
            }
        }
        scope.launch {
            val alreadySignedIn = runCatching { jsIsSignedIn() }.getOrDefault(false)
            if (!alreadySignedIn) {
                runCatching { jsSignInAnonymously().await<JsAny?>() }
                    .onFailure { jsConsoleError("[Auth] Anonymous sign-in failed: $it") }
            }
        }
    }

    override suspend fun getIdToken(): String? = runCatching { jsGetIdToken().await<JsAny?>()?.toString() }.getOrNull()

    override suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit> =
        performLink(
            primary = { jsLinkWithEmail(email, password).await<JsAny?>() },
            fallback = { jsSignInWithEmail(email, password).await<JsAny?>() },
        )

    override suspend fun linkWithGoogle(): Result<Unit> = runCatching { jsSignInWithGoogle().await<JsAny?>() }.map { }

    override suspend fun linkWithApple(): Result<Unit> = runCatching { jsSignInWithApple().await<JsAny?>() }.map { }

    private suspend fun performLink(
        primary: suspend () -> JsAny?,
        fallback: suspend () -> JsAny?,
    ): Result<Unit> =
        runCatching {
            runCatching { primary() }.getOrElse { ex ->
                if (ex.message?.contains("credential-already-in-use", ignoreCase = true) == true ||
                    ex.message?.contains("EMAIL_EXISTS", ignoreCase = true) == true
                ) {
                    fallback()
                } else {
                    throw ex
                }
            }
            Unit
        }
}
