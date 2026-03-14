package com.ifochka.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseAuthRepository(
    private val onUserSync: suspend () -> Unit,
    private val scope: CoroutineScope,
) : AuthRepository {
    // On JVM desktop without appId, Firebase.auth throws — detect eagerly.
    // On Android and future iOS, Firebase is always available via the platform config file.
    private val isFirebaseAvailable = runCatching { Firebase.auth }.isSuccess

    // Start as Anonymous when Firebase is active so the gate is enforced immediately,
    // before the auth handshake completes. Non-configured JVM desktop gets SignedIn
    // (no gate — desktop is a developer/admin surface).
    private val _authState = MutableStateFlow<AuthState>(
        if (isFirebaseAvailable) AuthState.Anonymous else AuthState.SignedIn(null),
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun start() {
        println("[Auth] Firebase available: $isFirebaseAvailable")
        if (isFirebaseAvailable) {
            if (AuthConfig.current.useEmulator) {
                runCatching { Firebase.auth.useEmulator("localhost", 9099) }
            }
            scope.launch {
                val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
                println("[Auth] Current user on start: uid=${currentUser?.uid} anonymous=${currentUser?.isAnonymous}")
                if (currentUser == null) {
                    println("[Auth] No user — signing in anonymously")
                    runCatching { Firebase.auth.signInAnonymously() }
                        .onSuccess { println("[Auth] Anonymous sign-in ok") }
                        .onFailure { println("[Auth] Anonymous sign-in failed: $it") }
                }
            }
            scope.launch {
                runCatching {
                    Firebase.auth.authStateChanged.collect { user ->
                        val newState = if (user?.isAnonymous == false) {
                            AuthState.SignedIn(user?.email)
                        } else {
                            AuthState.Anonymous
                        }
                        println("[Auth] authStateChanged: uid=${user?.uid} anonymous=${user?.isAnonymous} → $newState")
                        _authState.value = newState
                        if (user != null) {
                            onUserSync()
                                .also { println("[Auth] syncUser ok") }
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

    override suspend fun linkWithGoogle(): Result<Unit> {
        println("[Auth] linkWithGoogle: acquiring credential")
        val credential = getGoogleCredential()
        if (credential == null) {
            println("[Auth] linkWithGoogle: credential is null (client ID not configured?)")
            return Result.success(Unit)
        }
        println("[Auth] linkWithGoogle: credential obtained, linking")
        return linkWithCredential(credential)
    }

    override suspend fun linkWithApple(): Result<Unit> {
        val credential = getAppleCredential() ?: return Result.success(Unit)
        return linkWithCredential(credential)
    }

    private suspend fun linkWithCredential(credential: AuthCredential): Result<Unit> {
        val current = Firebase.auth.currentUser
            ?: return Result.failure(IllegalStateException("No current user"))
        println("[Auth] linkWithCredential: uid=${current.uid} anonymous=${current.isAnonymous}")
        return try {
            current.linkWithCredential(credential)
            println("[Auth] linkWithCredential: linked ok")
            Result.success(Unit)
        } catch (_: FirebaseAuthUserCollisionException) {
            // UC4: credential already linked to another account — switch to that account directly.
            println("[Auth] linkWithCredential: collision — attempting direct sign-in")
            trySignInOnCollision(credential)
        } catch (e: Exception) {
            println("[Auth] linkWithCredential: failed — ${e.message}")
            Result.failure(e)
        }
    }
}

private suspend fun trySignInOnCollision(credential: AuthCredential): Result<Unit> =
    try {
        val result = Firebase.auth.signInWithCredential(credential)
        println("[Auth] Switched to existing account via direct sign-in, uid=${result.user?.uid}")
        Result.success(Unit)
    } catch (e: FirebaseAuthUserCollisionException) {
        // The credential belongs to an account that uses a different provider.
        // Auto-linking two separate identities without explicit user consent is dangerous — surface the error.
        println("[Auth] Provider conflict — cannot auto-link: ${e.message}")
        Result.failure(
            Exception(
                "This email is already registered with a different sign-in method. " +
                    "Please use your original sign-in method to continue.",
            ),
        )
    } catch (e: Exception) {
        println("[Auth] trySignInOnCollision: failed — ${e.message}")
        Result.failure(e)
    }
