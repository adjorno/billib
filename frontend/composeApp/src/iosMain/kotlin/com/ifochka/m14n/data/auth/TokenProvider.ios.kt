package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

actual suspend fun getFirebaseToken(): String? =
    runCatching { Firebase.auth.currentUser?.getIdToken(false) }.getOrNull()
