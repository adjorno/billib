package com.ifochka.auth

import dev.gitlive.firebase.auth.AuthCredential

// Native Apple Sign-In requires ASAuthorizationController — tracked as follow-up.
actual suspend fun getAppleCredential(): AuthCredential? = null
