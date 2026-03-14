package com.ifochka.auth

data class FirebaseAuthConfig(
    val apiKey: String,
    val projectId: String,
    val appId: String,
    val authDomain: String,
    val googleWebClientId: String,
    val apiBaseUrl: String = "",
    val appleServiceId: String = "",
    val useEmulator: Boolean = false,
)
