package com.ifochka.m14n.data.auth

import com.ifochka.m14n.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

actual suspend fun getFirebaseToken(): String? {
    if (BuildKonfig.FIREBASE_APP_ID.isEmpty()) return null
    return runCatching { Firebase.auth.currentUser?.getIdToken(false) }.getOrNull()
}
