package com.ifochka.m14n.data.auth

// No Firebase JS SDK interop yet; Ktor falls back to FIREBASE_API_KEY.
actual suspend fun getFirebaseToken(): String? = null
