package com.ifochka.auth

import kotlinx.coroutines.CoroutineScope

actual fun createFirebaseAuthRepository(
    onUserSync: suspend () -> Unit,
    scope: CoroutineScope,
): AuthRepository = WasmFirebaseAuthRepository(onUserSync = onUserSync, scope = scope).also { it.start() }
