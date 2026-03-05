package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.auth.AuthCredential

// Apple Sign-In on Android requires an Activity context for the system browser OAuth flow.
// Tracked as a follow-up GitHub issue — implement once Apple Service ID + private key are registered.
actual suspend fun getAppleCredential(): AuthCredential? = null
