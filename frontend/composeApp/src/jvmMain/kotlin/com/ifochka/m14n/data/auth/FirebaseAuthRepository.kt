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
    // Firebase is initialized when an app ID is provided OR when using the emulator.
    private val initialized = BuildKonfig.FIREBASE_APP_ID.isNotEmpty() || BuildKonfig.USE_FIREBASE_EMULATOR
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _authState = MutableStateFlow<AuthState>(
        if (initialized) AuthState.Loading else AuthState.SignedIn,
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        println(
            "[Auth] FirebaseAuthRepository: initialized=$initialized emulator=${BuildKonfig.USE_FIREBASE_EMULATOR} appId='${BuildKonfig.FIREBASE_APP_ID}'",
        )
        if (initialized) {
            if (BuildKonfig.USE_FIREBASE_EMULATOR) {
                println("[Auth] Connecting to emulator at localhost:9099")
                Firebase.auth.useEmulator("localhost", 9099)
            }
            scope.launch {
                println("[Auth] Calling signInAnonymously...")
                val result = runCatching { Firebase.auth.signInAnonymously() }
                println("[Auth] signInAnonymously result: ${result.map { "uid=${it.user?.uid}" }}")
            }
            scope.launch {
                Firebase.auth.authStateChanged.collect { user ->
                    println("[Auth] authStateChanged: uid=${user?.uid} isAnonymous=${user?.isAnonymous}")
                    _authState.value = when {
                        user == null -> AuthState.Loading
                        user.isAnonymous -> AuthState.Anonymous
                        else -> AuthState.SignedIn
                    }
                    if (user != null) {
                        val syncResult = runCatching { api.syncUser() }
                        println("[Auth] syncUser result: $syncResult")
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
