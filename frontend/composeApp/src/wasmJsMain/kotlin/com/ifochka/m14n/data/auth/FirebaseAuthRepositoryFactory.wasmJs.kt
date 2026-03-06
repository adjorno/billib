package com.ifochka.m14n.data.auth

import com.ifochka.m14n.data.api.M14nApi
import kotlinx.coroutines.CoroutineScope

actual fun createFirebaseAuthRepository(
    api: M14nApi,
    scope: CoroutineScope,
): AuthRepository = WasmFirebaseAuthRepository(api = api, scope = scope)
