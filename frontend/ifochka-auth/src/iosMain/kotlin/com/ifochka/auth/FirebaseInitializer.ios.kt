package com.ifochka.auth

// Firebase is initialized automatically via GoogleService-Info.plist + the Firebase iOS SDK.
actual fun initFirebase(config: FirebaseAuthConfig) {
    AuthConfig.current = config
}
