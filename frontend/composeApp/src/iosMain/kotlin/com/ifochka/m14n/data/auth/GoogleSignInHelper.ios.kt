package com.ifochka.m14n.data.auth

import cocoapods.GoogleSignIn.GIDSignIn
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual suspend fun getGoogleCredential(): AuthCredential? {
    val rootVC = (UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene)
        ?.keyWindow?.rootViewController ?: return null
    val (idToken, accessToken) = suspendCoroutine { cont ->
        GIDSignIn.sharedInstance
            .signInWithPresentingViewController(rootVC, null, null) { result, _ ->
                cont.resume(result?.user?.idToken?.tokenString to result?.user?.accessToken?.tokenString)
            }
    }
    if (idToken == null || accessToken == null) return null
    return GoogleAuthProvider.credential(
        idToken = idToken,
        accessToken = accessToken,
    )
}
