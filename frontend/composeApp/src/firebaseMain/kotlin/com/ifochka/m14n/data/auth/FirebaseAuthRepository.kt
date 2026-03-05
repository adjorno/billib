package com.ifochka.m14n.data.auth

import com.ifochka.m14n.BuildKonfig
import com.ifochka.m14n.data.api.M14nApi
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseAuthRepository(
    private val api: M14nApi,
) : AuthRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // On JVM desktop without FIREBASE_APP_ID, Firebase.auth throws — detect eagerly.
    // On Android and future iOS, Firebase is always available via the platform config file.
    private val isFirebaseAvailable = runCatching { Firebase.auth }.isSuccess

    // Start as Anonymous when Firebase is active so the gate is enforced immediately,
    // before the auth handshake completes. Non-configured JVM desktop gets SignedIn
    // (no gate — desktop is a developer/admin surface).
    private val _authState = MutableStateFlow<AuthState>(
        if (isFirebaseAvailable) AuthState.Anonymous else AuthState.SignedIn,
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        if (isFirebaseAvailable) {
            if (BuildKonfig.USE_FIREBASE_EMULATOR) {
                runCatching { Firebase.auth.useEmulator("localhost", 9099) }
            }
            scope.launch {
                val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
                if (currentUser == null) {
                    runCatching { Firebase.auth.signInAnonymously() }
                        .onFailure { println("[Auth] Anonymous sign-in failed: $it") }
                }
            }
            scope.launch {
                runCatching {
                    Firebase.auth.authStateChanged.collect { user ->
                        _authState.value = when {
                            user == null -> AuthState.Anonymous
                            user.isAnonymous -> AuthState.Anonymous
                            else -> AuthState.SignedIn
                        }
                        if (user != null) {
                            runCatching { api.syncUser() }
                                .onFailure { println("[Auth] syncUser failed: $it") }
                        }
                    }
                }.onFailure { println("[Auth] authStateChanged collection failed: $it") }
            }
        }
    }

    override suspend fun getIdToken(): String? =
        if (isFirebaseAvailable) {
            runCatching { Firebase.auth.currentUser?.getIdToken(false) }.getOrNull()
        } else {
            null
        }

    override suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit> = linkWithCredential(EmailAuthProvider.credential(email = email, password = password))

    override suspend fun linkWithGoogle(): Result<Unit> {
        val credential = getGoogleCredential() ?: return Result.success(Unit)
        return linkWithCredential(credential)
    }

    override suspend fun linkWithApple(): Result<Unit> {
        val credential = getAppleCredential() ?: return Result.success(Unit)
        return linkWithCredential(credential)
    }

    private suspend fun linkWithCredential(credential: AuthCredential): Result<Unit> =
        runCatching {
            val current = checkNotNull(Firebase.auth.currentUser) { "No current user" }
            runCatching { current.linkWithCredential(credential) }.getOrElse {
                // Credential already exists — sign into the existing account instead.
                Firebase.auth.signInWithCredential(credential)
            }
            Unit
        }
}
