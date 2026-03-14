package com.ifochka.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

actual suspend fun getFirebaseToken(): String? {
    if (AuthConfig.current.appId.isEmpty()) {
        if (LogFlags.AUTH) println("[Auth] getFirebaseToken: no app ID, returning null")
        return null
    }
    val user = Firebase.auth.currentUser
    if (LogFlags.AUTH) println("[Auth] getFirebaseToken: user=${user?.uid?.take(8)} isAnon=${user?.isAnonymous}")
    if (user == null) return null
    return runCatching { user.getIdToken(false) }.also { result ->
        if (LogFlags.AUTH) {
            println(
                "[Auth] getFirebaseToken: result=${result.map { t ->
                    if (t != null) "JWT(len=${t.length})" else "null"
                }}",
            )
        }
    }.getOrNull()
}
