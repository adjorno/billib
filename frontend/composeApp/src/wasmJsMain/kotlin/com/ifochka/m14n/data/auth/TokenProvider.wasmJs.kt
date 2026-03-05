package com.ifochka.m14n.data.auth

import kotlinx.coroutines.await
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
actual suspend fun getFirebaseToken(): String? = runCatching { jsGetIdToken().await<JsAny?>()?.toString() }.getOrNull()
