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
    init {
        Firebase.auth // crash early with a clear message if Firebase is not configured
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Anonymous)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun start() {
        if (AuthConfig.current.useEmulator) {
            runCatching { Firebase.auth.useEmulator("localhost", 9099) }
        }
        scope.launch {
            val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
            if (LogFlags.AUTH) {
                println(
                    "[Auth] Current user on start: uid=${currentUser?.uid} anonymous=${currentUser?.isAnonymous}",
                )
            }
            if (currentUser == null) {
                if (LogFlags.AUTH) println("[Auth] No user — signing in anonymously")
                runCatching { Firebase.auth.signInAnonymously() }
                    .onSuccess { if (LogFlags.AUTH) println("[Auth] Anonymous sign-in ok") }
                    .onFailure { if (LogFlags.AUTH) println("[Auth] Anonymous sign-in failed: $it") }
            } else {
                // On JVM, authStateChanged may not fire reliably — sync state from stored user.
                syncStateFromCurrentUser()
            }
        }
        scope.launch {
            runCatching {
                Firebase.auth.authStateChanged.collect { user ->
                    val newState = if (user?.isAnonymous == false) {
                        val email = user.email
                            ?: user.providerData.firstNotNullOfOrNull { it.email }
                            ?: error(
                                "Authenticated user uid=${user.uid} has no email in user.email or providerData",
                            )
                        AuthState.SignedIn(email)
                    } else {
                        AuthState.Anonymous
                    }
                    if (LogFlags.AUTH) {
                        println(
                            "[Auth] authStateChanged: uid=${user?.uid} anonymous=${user?.isAnonymous} → $newState",
                        )
                    }
                    _authState.value = newState
                    if (user != null) {
                        onUserSync()
                            .also { if (LogFlags.AUTH) println("[Auth] syncUser ok") }
                    }
                }
            }.onFailure { if (LogFlags.AUTH) println("[Auth] authStateChanged collection failed: $it") }
        }
    }

    override suspend fun getIdToken(): String? =
        runCatching { Firebase.auth.currentUser?.getIdToken(false) }.getOrNull()

    override suspend fun linkWithGoogle(): Result<Unit> {
        if (LogFlags.AUTH) println("[Auth] linkWithGoogle: acquiring credential")
        val credential = getGoogleCredential()
        if (credential == null) {
            if (LogFlags.AUTH) println("[Auth] linkWithGoogle: sign-in handled directly — syncing state")
            syncStateFromCurrentUser()
            return Result.success(Unit)
        }
        if (LogFlags.AUTH) println("[Auth] linkWithGoogle: credential obtained, linking")
        return linkWithCredential(credential)
    }

    override suspend fun linkWithApple(): Result<Unit> {
        val credential = getAppleCredential() ?: return Result.success(Unit)
        val current = Firebase.auth.currentUser
            ?: return Result.failure(IllegalStateException("No current user"))
        if (LogFlags.AUTH) println("[Auth] linkWithApple: uid=${current.uid} anonymous=${current.isAnonymous}")
        return try {
            current.linkWithCredential(credential)
            if (LogFlags.AUTH) println("[Auth] linkWithApple: linked ok")
            syncStateFromCurrentUser()
            Result.success(Unit)
        } catch (_: FirebaseAuthUserCollisionException) {
            // UC4: Apple tokens are one-time-use — the nonce was consumed by linkWithCredential.
            // Re-prompt the user for a fresh Apple credential before switching accounts.
            if (LogFlags.AUTH) println("[Auth] linkWithApple: collision — requesting fresh Apple credential")
            switchToExistingAppleAccount()
        } catch (e: Exception) {
            if (LogFlags.AUTH) println("[Auth] linkWithApple: failed — ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun switchToExistingAppleAccount(): Result<Unit> {
        val freshCredential = getAppleCredential() ?: return Result.success(Unit)
        return try {
            val result = Firebase.auth.signInWithCredential(freshCredential)
            if (LogFlags.AUTH) println("[Auth] linkWithApple: switched to existing account, uid=${result.user?.uid}")
            syncStateFromCurrentUser()
            Result.success(Unit)
        } catch (e: FirebaseAuthUserCollisionException) {
            if (LogFlags.AUTH) println("[Auth] linkWithApple: provider conflict — ${e.message}")
            Result.failure(
                Exception(
                    "This email is already registered with a different sign-in method. " +
                        "Please use your original sign-in method to continue.",
                ),
            )
        } catch (e: Exception) {
            if (LogFlags.AUTH) println("[Auth] linkWithApple: fresh sign-in failed — ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun linkWithCredential(credential: AuthCredential): Result<Unit> {
        val current = Firebase.auth.currentUser
            ?: return Result.failure(IllegalStateException("No current user"))
        if (LogFlags.AUTH) println("[Auth] linkWithCredential: uid=${current.uid} anonymous=${current.isAnonymous}")
        return try {
            current.linkWithCredential(credential)
            if (LogFlags.AUTH) println("[Auth] linkWithCredential: linked ok")
            Result.success(Unit)
        } catch (_: FirebaseAuthUserCollisionException) {
            if (LogFlags.AUTH) println("[Auth] linkWithCredential: collision — attempting direct sign-in")
            trySignInOnCollision(credential)
        } catch (e: Exception) {
            if (LogFlags.AUTH) println("[Auth] linkWithCredential: failed — ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fallback for platforms where authStateChanged does not fire reliably (JVM desktop).
     * Reads the current user's email directly and updates _authState + triggers onUserSync.
     * No-op when the current user is anonymous (no email) — state correctly stays Anonymous.
     */
    private suspend fun syncStateFromCurrentUser() {
        val user = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (LogFlags.AUTH) println("[Auth] syncStateFromCurrentUser: uid=${runCatching { user?.uid }.getOrNull()}")
        if (user == null) return
        // pendingDirectSignInEmail is set by JVM sign-in helpers because firebase-java-sdk
        // does not populate FirebaseUser.email after signInWithCustomToken.
        val email = pendingDirectSignInEmail?.also { pendingDirectSignInEmail = null }
            ?: runCatching { user.email }.getOrNull()
            ?: runCatching { user.providerData.firstNotNullOfOrNull { it.email } }.getOrNull()
        if (email.isNullOrBlank()) {
            if (LogFlags.AUTH) println("[Auth] syncStateFromCurrentUser: no email — user is anonymous, skipping")
            return
        }
        if (LogFlags.AUTH) println("[Auth] syncStateFromCurrentUser: email=$email → SignedIn")
        _authState.value = AuthState.SignedIn(email)
        onUserSync()
    }
}

private suspend fun trySignInOnCollision(credential: AuthCredential): Result<Unit> =
    try {
        val result = Firebase.auth.signInWithCredential(credential)
        if (LogFlags.AUTH) println("[Auth] Switched to existing account via direct sign-in, uid=${result.user?.uid}")
        Result.success(Unit)
    } catch (e: FirebaseAuthUserCollisionException) {
        // The credential belongs to an account that uses a different provider.
        // Auto-linking two separate identities without explicit user consent is dangerous — surface the error.
        if (LogFlags.AUTH) println("[Auth] Provider conflict — cannot auto-link: ${e.message}")
        Result.failure(
            Exception(
                "This email is already registered with a different sign-in method. " +
                    "Please use your original sign-in method to continue.",
            ),
        )
    } catch (e: Exception) {
        if (LogFlags.AUTH) println("[Auth] trySignInOnCollision: failed — ${e.message}")
        Result.failure(e)
    }
