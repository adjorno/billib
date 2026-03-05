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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        if (BuildKonfig.USE_FIREBASE_EMULATOR) {
            Firebase.auth.useEmulator("localhost", 9099)
        }
        scope.launch { runCatching { Firebase.auth.signInAnonymously() } }
        scope.launch {
            Firebase.auth.authStateChanged.collect { user ->
                _authState.value = when {
                    user == null -> AuthState.Loading
                    user.isAnonymous -> AuthState.Anonymous
                    else -> AuthState.SignedIn
                }
                if (user != null) {
                    runCatching { api.syncUser() }
                }
            }
        }
    }

    override suspend fun getIdToken(): String? =
        runCatching { Firebase.auth.currentUser?.getIdToken(false) }.getOrNull()

    override suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit> {
        val credential = EmailAuthProvider.credential(email = email, password = password)
        return linkWithCredential(credential)
    }

    override suspend fun linkWithCredential(credential: AuthCredential): Result<Unit> =
        runCatching {
            val current = checkNotNull(Firebase.auth.currentUser) { "No current user" }
            runCatching { current.linkWithCredential(credential) }.getOrElse {
                // Credential already exists — sign into the existing account instead.
                Firebase.auth.signInWithCredential(credential)
            }
            Unit
        }
}
