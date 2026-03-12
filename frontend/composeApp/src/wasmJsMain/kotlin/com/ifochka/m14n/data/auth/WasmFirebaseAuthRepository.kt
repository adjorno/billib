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
        jsOnAuthStateChanged { isSignedIn ->
            val newState = if (isSignedIn) AuthState.SignedIn else AuthState.Anonymous
            jsConsoleLog("[Auth] State → $newState")
            _authState.value = newState
            scope.launch {
                jsConsoleLog("[Auth] Calling syncUser...")
                runCatching { api.syncUser() }
                    .onSuccess { jsConsoleLog("[Auth] syncUser OK") }
                    .onFailure { jsConsoleError("[Auth] syncUser failed: $it") }
            }
        }
        scope.launch {
            jsConsoleLog("[Auth] Checking for pending redirect result...")
            runCatching { jsGetRedirectResult().await<JsAny?>() }
                .onSuccess { jsConsoleLog("[Auth] getRedirectResult complete") }
                .onFailure { jsConsoleError("[Auth] getRedirectResult failed: $it") }
            val alreadySignedIn = runCatching { jsIsSignedIn() }.getOrDefault(false)
            if (alreadySignedIn) {
                jsConsoleLog("[Auth] Session restored — already signed in")
            } else {
                jsConsoleLog("[Auth] No session — signing in anonymously")
                runCatching { jsSignInAnonymously().await<JsAny?>() }
                    .onSuccess { jsConsoleLog("[Auth] Anonymous sign-in complete") }
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

    override suspend fun linkWithGoogle(): Result<Unit> {
        jsConsoleLog("[Auth] linkWithGoogle → initiating popup...")
        return runCatching { jsSignInWithGoogle().await<JsAny?>() }
            .onSuccess {
                val wasAlreadySignedIn = _authState.value == AuthState.SignedIn
                _authState.value = AuthState.SignedIn // always set immediately
                if (!wasAlreadySignedIn) {
                    // UC3: linkWithPopup does not fire onAuthStateChanged — manual sync needed
                    jsConsoleLog("[Auth] linkWithGoogle popup completed — syncing state")
                    runCatching { api.syncUser() }
                        .onSuccess { jsConsoleLog("[Auth] syncUser OK") }
                        .onFailure { jsConsoleError("[Auth] syncUser failed: $it") }
                } else {
                    // UC4: signInWithCredential already fired onAuthStateChanged → already synced
                    jsConsoleLog(
                        "[Auth] linkWithGoogle popup completed — state already SignedIn (UC4), skipping manual sync",
                    )
                }
            }
            .onFailure { jsConsoleError("[Auth] linkWithGoogle failed: $it") }
            .map { }
    }

    override suspend fun linkWithApple(): Result<Unit> {
        jsConsoleLog("[Auth] linkWithApple → initiating redirect to Apple...")
        return runCatching { jsSignInWithApple().await<JsAny?>() }
            .onSuccess { jsConsoleLog("[Auth] linkWithApple redirect initiated — auth completes on next page load") }
            .onFailure { jsConsoleError("[Auth] linkWithApple failed: $it") }
            .map { }
    }

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
