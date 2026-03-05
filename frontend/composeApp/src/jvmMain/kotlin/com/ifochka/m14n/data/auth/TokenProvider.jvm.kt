package com.ifochka.m14n.data.auth

import com.ifochka.m14n.BuildKonfig
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

actual suspend fun getFirebaseToken(): String? {
    if (BuildKonfig.FIREBASE_APP_ID.isEmpty()) {
        println("[Auth] getFirebaseToken: no app ID, returning null")
        return null
    }
    val user = Firebase.auth.currentUser
    println("[Auth] getFirebaseToken: user=${user?.uid?.take(8)} isAnon=${user?.isAnonymous}")
    if (user == null) return null
    return runCatching { user.getIdToken(false) }.also { result ->
        println(
            "[Auth] getFirebaseToken: result=${result.map { t ->
                if (t != null) "JWT(len=${t.length})" else "null"
            }}",
        )
    }.getOrNull()
}
