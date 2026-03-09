@file:OptIn(ExperimentalWasmJsInterop::class)

package com.ifochka.m14n.data.auth

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise

@JsFun("(a, b, c, d) => jsFirebaseInit(a, b, c, d)")
internal external fun jsFirebaseInit(
    apiKey: String,
    projectId: String,
    appId: String,
    authDomain: String,
)

@JsFun("() => jsUseFirebaseEmulator()")
internal external fun jsUseFirebaseEmulator()

@JsFun("() => jsSignInAnonymously()")
internal external fun jsSignInAnonymously(): Promise<JsAny?>

@JsFun("() => jsGetIdToken()")
internal external fun jsGetIdToken(): Promise<JsAny?>

@JsFun("() => jsGetIsAnonymous()")
internal external fun jsGetIsAnonymous(): Boolean

@JsFun("() => jsIsSignedIn()")
internal external fun jsIsSignedIn(): Boolean

@JsFun("(e, p) => jsLinkWithEmail(e, p)")
internal external fun jsLinkWithEmail(
    email: String,
    password: String,
): Promise<JsAny?>

@JsFun("(e, p) => jsSignInWithEmail(e, p)")
internal external fun jsSignInWithEmail(
    email: String,
    password: String,
): Promise<JsAny?>

@JsFun("() => jsSignInWithGoogle()")
internal external fun jsSignInWithGoogle(): Promise<JsAny?>

@JsFun("() => jsSignInWithApple()")
internal external fun jsSignInWithApple(): Promise<JsAny?>

@JsFun("() => jsGetRedirectResult()")
internal external fun jsGetRedirectResult(): Promise<JsAny?>

@JsFun("(callback) => jsOnAuthStateChanged(callback)")
internal external fun jsOnAuthStateChanged(callback: (Boolean) -> Unit)

@JsFun("(msg) => jsConsoleLog(msg)")
internal external fun jsConsoleLog(msg: String)

@JsFun("(msg) => jsConsoleError(msg)")
internal external fun jsConsoleError(msg: String)
