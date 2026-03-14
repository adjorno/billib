package com.ifochka.auth

// Firebase is initialized automatically via google-services.json + the Firebase Android SDK.
actual fun initFirebase(config: FirebaseAuthConfig) {
    AuthConfig.current = config
}
