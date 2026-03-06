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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        scope.launch {
            val alreadySignedIn = runCatching { jsIsSignedIn() }.getOrDefault(false)
            if (!alreadySignedIn) {
                runCatching { jsSignInAnonymously().await<JsAny?>() }
                    .onFailure { jsConsoleError("[Auth] Anonymous sign-in failed: $it") }
            }
            _authState.value = if (!jsGetIsAnonymous()) AuthState.SignedIn else AuthState.Anonymous
            runCatching { api.syncUser() }
                .onFailure { jsConsoleError("[Auth] syncUser failed: $it") }
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

    override suspend fun linkWithGoogle(): Result<Unit> =
        performLink(
            primary = { jsSignInWithGoogle().await<JsAny?>() },
            fallback = { jsSignInWithGoogleForced().await<JsAny?>() },
        )

    override suspend fun linkWithApple(): Result<Unit> =
        performLink(
            primary = { jsSignInWithApple().await<JsAny?>() },
            fallback = { jsSignInWithAppleForced().await<JsAny?>() },
        )

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
            _authState.value = AuthState.SignedIn
            runCatching { api.syncUser() }.onFailure { jsConsoleError("[Auth] syncUser failed: $it") }
            Unit
        }
}
