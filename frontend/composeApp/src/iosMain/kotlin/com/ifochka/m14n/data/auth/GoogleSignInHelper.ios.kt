package com.ifochka.m14n.data.auth

import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual suspend fun getGoogleCredential(): AuthCredential? {
    val delegate = GoogleSignInDelegateHolder.delegate ?: return null
    var idToken: String? = null
    var accessToken: String? = null
    suspendCancellableCoroutine { cont ->
        delegate.signIn { token, access ->
            idToken = token
            accessToken = access
            cont.resume(Unit)
        }
    }
    val resolvedIdToken = idToken ?: return null
    val resolvedAccessToken = accessToken ?: return null
    return GoogleAuthProvider.credential(
        idToken = resolvedIdToken,
        accessToken = resolvedAccessToken,
    )
}
