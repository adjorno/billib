package com.ifochka.m14n.data.auth

import com.ifochka.m14n.BuildKonfig
import com.ifochka.m14n.data.api.M14nApi
import dev.gitlive.firebase.Firebase
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
    // When FIREBASE_APP_ID is empty the SDK is not initialized; treat as SignedIn (API key fallback).
    private val initialized = BuildKonfig.FIREBASE_APP_ID.isNotEmpty()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _authState = MutableStateFlow<AuthState>(
        if (initialized) AuthState.Loading else AuthState.SignedIn,
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        if (initialized) {
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
    }

    override suspend fun getIdToken(): String? =
        if (initialized) {
            runCatching { Firebase.auth.currentUser?.getIdToken(false) }.getOrNull()
        } else {
            null
        }

    override suspend fun linkWithEmailCredential(
        email: String,
        password: String,
    ): Result<Unit> =
        if (initialized) {
            runCatching {
                val credential = EmailAuthProvider.credential(email, password)
                checkNotNull(Firebase.auth.currentUser) { "No current user" }
                    .linkWithCredential(credential)
                Unit
            }
        } else {
            Result.success(Unit)
        }
}
