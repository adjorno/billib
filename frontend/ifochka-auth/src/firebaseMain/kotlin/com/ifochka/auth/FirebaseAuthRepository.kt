package com.ifochka.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.EmailAuthProvider
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

    init {
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

    override suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit> = linkWithCredential(EmailAuthProvider.credential(email = email, password = password))

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

    private suspend fun linkWithCredential(credential: AuthCredential): Result<Unit> =
        runCatching {
            val current = checkNotNull(Firebase.auth.currentUser) { "No current user" }
            println("[Auth] linkWithCredential: uid=${current.uid} anonymous=${current.isAnonymous}")
            runCatching { current.linkWithCredential(credential) }
                .onSuccess { println("[Auth] linkWithCredential: linked ok, uid=${it.user?.uid}") }
                .getOrElse { exception ->
                    println("[Auth] linkWithCredential: failed (${exception.message})")
                    if (exception.message?.contains("credential-already-in-use", ignoreCase = true) == true ||
                        exception.message?.contains("EMAIL_EXISTS", ignoreCase = true) == true
                    ) {
                        println("[Auth] linkWithCredential: credential already in use — signing in directly")
                        Firebase.auth.signInWithCredential(credential)
                            .also { println("[Auth] signInWithCredential ok, uid=${it.user?.uid}") }
                    } else {
                        throw exception
                    }
                }
            Unit
        }.onFailure { println("[Auth] linkWithCredential: unhandled error: $it") }
}
