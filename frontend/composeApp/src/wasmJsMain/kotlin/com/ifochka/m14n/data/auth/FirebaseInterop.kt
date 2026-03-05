package com.ifochka.m14n.data.auth

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(a, b, c, d) => jsFirebaseInit(a, b, c, d)")
internal external fun jsFirebaseInit(
    apiKey: String,
    projectId: String,
    appId: String,
    authDomain: String,
)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => jsUseFirebaseEmulator()")
internal external fun jsUseFirebaseEmulator()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => jsSignInAnonymously()")
internal external fun jsSignInAnonymously(): Promise<JsAny?>

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => jsGetIdToken()")
internal external fun jsGetIdToken(): Promise<JsAny?>

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => jsGetIsAnonymous()")
internal external fun jsGetIsAnonymous(): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => jsIsSignedIn()")
internal external fun jsIsSignedIn(): Boolean
