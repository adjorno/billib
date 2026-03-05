package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.auth.AuthCredential

// Apple Sign-In on JVM desktop via system browser loopback OAuth — deferred to a follow-up GitHub issue.
// Implement once Apple Service ID + private key are registered.
actual suspend fun getAppleCredential(): AuthCredential? = null
