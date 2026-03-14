package com.ifochka.auth

actual fun initFirebase(config: FirebaseAuthConfig) {
    AuthConfig.current = config
    jsFirebaseInit(
        apiKey = config.apiKey,
        projectId = config.projectId,
        appId = config.appId,
        authDomain = config.authDomain,
    )
    if (config.useEmulator) jsUseFirebaseEmulator()
}
