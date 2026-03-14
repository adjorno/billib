package com.ifochka.auth

import kotlinx.coroutines.CoroutineScope

expect fun createFirebaseAuthRepository(
    onUserSync: suspend () -> Unit,
    scope: CoroutineScope,
): AuthRepository
