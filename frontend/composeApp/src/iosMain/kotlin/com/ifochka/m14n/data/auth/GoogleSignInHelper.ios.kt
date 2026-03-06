package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.auth.AuthCredential

// Native Google Sign-In on iOS requires GoogleSignIn-iOS SDK + URL scheme — tracked as follow-up.
actual suspend fun getGoogleCredential(): AuthCredential? = null
