package com.ifochka.m14n.data.auth

import com.ifochka.m14n.BuildKonfig

actual fun initFirebase() {
    jsFirebaseInit(
        apiKey = BuildKonfig.FIREBASE_API_KEY,
        projectId = BuildKonfig.FIREBASE_PROJECT_ID,
        appId = BuildKonfig.FIREBASE_APP_ID,
        authDomain = BuildKonfig.FIREBASE_AUTH_DOMAIN,
    )
    if (BuildKonfig.USE_FIREBASE_EMULATOR) jsUseFirebaseEmulator()
}
