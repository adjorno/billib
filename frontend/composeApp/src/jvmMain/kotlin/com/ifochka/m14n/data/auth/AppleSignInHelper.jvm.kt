package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.auth.AuthCredential

// Apple Sign-In on JVM desktop via system browser loopback OAuth — deferred to a future iteration.
// Tracked as a follow-up GitHub issue — implement once Apple Service ID + private key are registered.
actual suspend fun getAppleCredential(): AuthCredential? = null
