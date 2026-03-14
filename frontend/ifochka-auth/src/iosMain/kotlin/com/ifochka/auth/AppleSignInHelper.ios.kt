@file:OptIn(ExperimentalForeignApi::class)

package com.ifochka.auth

import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlin.coroutines.resume

private var activeDelegate: AppleSignInDelegate? = null
private var activePresentationContext: PresentationContextProvider? = null

actual suspend fun getAppleCredential(): AuthCredential? {
    val nonce = randomNonce()
    println("[Apple] getAppleCredential: starting, nonce prefix=${nonce.take(8)}")

    val idToken = suspendCancellableCoroutine<String?> { cont ->
        val delegate = AppleSignInDelegate { cont.resume(it) }
        val presentationContext = PresentationContextProvider()
        activeDelegate = delegate
        activePresentationContext = presentationContext

        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeEmail, ASAuthorizationScopeFullName)
            this.nonce = sha256(nonce)
        }
        println("[Apple] ASAuthorizationController: performRequests()")
        ASAuthorizationController(listOf(request)).apply {
            this.delegate = delegate
            this.presentationContextProvider = presentationContext
            performRequests()
        }
        cont.invokeOnCancellation {
            println("[Apple] getAppleCredential: cancelled")
            activeDelegate = null
            activePresentationContext = null
        }
    } ?: run {
        println("[Apple] getAppleCredential: idToken is null (user cancelled or delegate error)")
        return null
    }

    println("[Apple] getAppleCredential: idToken obtained (length=${idToken.length}), building credential")
    return OAuthProvider.credential(
        providerId = "apple.com",
        idToken = idToken,
        rawNonce = nonce,
    )
}

private class AppleSignInDelegate(
    private val onResult: (String?) -> Unit,
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol {
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization,
    ) {
        activeDelegate = null
        activePresentationContext = null
        val appleCredential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        val idToken = appleCredential?.identityToken
            ?.let { NSString.create(it, NSUTF8StringEncoding)?.toString() }
        println(
            "[Apple] delegate: didCompleteWithAuthorization — user=${appleCredential?.user} idToken=${if (idToken != null) "ok(${idToken.length})" else "null"}",
        )
        onResult(idToken)
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError,
    ) {
        activeDelegate = null
        activePresentationContext = null
        println(
            "[Apple] delegate: didCompleteWithError — code=${didCompleteWithError.code} domain=${didCompleteWithError.domain} msg=${didCompleteWithError.localizedDescription}",
        )
        onResult(null)
    }
}

private class PresentationContextProvider :
    NSObject(),
    ASAuthorizationControllerPresentationContextProvidingProtocol {
    override fun presentationAnchorForAuthorizationController(
        controller: ASAuthorizationController,
    ): ASPresentationAnchor = UIApplication.sharedApplication.keyWindow?.rootViewController?.view?.window
}

private fun randomNonce(length: Int = 32): String {
    val charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._"
    val randomBytes = ByteArray(length)
    randomBytes.usePinned { pinned ->
        val status = SecRandomCopyBytes(kSecRandomDefault, length.convert(), pinned.addressOf(0))
        if (status != errSecSuccess) error("SecRandomCopyBytes failed: $status")
    }
    return randomBytes.map { byte -> charset[(byte.toInt() and 0xFF) % charset.length] }.joinToString("")
}

private fun sha256(input: String): String {
    val inputData = input.encodeToByteArray()
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    inputData.usePinned { CC_SHA256(it.addressOf(0), inputData.size.convert(), digest.refTo(0)) }
    return digest.joinToString("") { byte -> byte.toInt().toString(16).padStart(2, '0') }
}
