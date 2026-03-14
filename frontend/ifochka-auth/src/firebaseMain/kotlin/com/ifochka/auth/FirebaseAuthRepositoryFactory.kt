package com.ifochka.auth

import kotlinx.coroutines.CoroutineScope

actual fun createFirebaseAuthRepository(
    onUserSync: suspend () -> Unit,
    scope: CoroutineScope,
): AuthRepository = FirebaseAuthRepository(onUserSync = onUserSync, scope = scope).also { it.start() }
