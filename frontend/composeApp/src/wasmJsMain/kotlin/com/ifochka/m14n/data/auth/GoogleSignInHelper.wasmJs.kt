package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.auth.AuthCredential

// wasmJs Firebase auth interop is deferred to a future iteration.
actual suspend fun getGoogleCredential(): AuthCredential? = null
