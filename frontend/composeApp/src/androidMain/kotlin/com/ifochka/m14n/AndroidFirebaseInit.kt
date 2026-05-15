package com.ifochka.m14n

import com.ifochka.auth.FirebaseAuthConfig
import com.ifochka.auth.initFirebase

fun initAndroidFirebase() {
    initFirebase(
        FirebaseAuthConfig(
            apiKey = BuildKonfig.FIREBASE_API_KEY,
            projectId = BuildKonfig.FIREBASE_PROJECT_ID,
            appId = BuildKonfig.FIREBASE_APP_ID,
            authDomain = BuildKonfig.FIREBASE_AUTH_DOMAIN,
            googleWebClientId = BuildKonfig.GOOGLE_WEB_CLIENT_ID,
            useEmulator = BuildKonfig.USE_FIREBASE_EMULATOR,
        ),
    )
}
