package com.ifochka.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.auth
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.ServerSocket
import java.net.URI
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

actual suspend fun getAppleCredential(): AuthCredential? {
    if (AuthConfig.current.appleServiceId.isEmpty()) return null
    return runCatching {
        val port = ServerSocket(0).use { it.localPort }
        val rawNonce = randomAppleBase64(32)
        val hashedNonce = sha256Hex(rawNonce)
        val state = buildAppleState(port = port, rawNonce = rawNonce)
        val appleCallbackBase = AuthConfig.current.appleCallbackUrl.trimEnd('/')
            .ifEmpty { AuthConfig.current.apiBaseUrl.trimEnd('/') }
        val redirectUri = "$appleCallbackBase/auth/apple/callback"
        val authUrl = buildAppleAuthUrl(
            serviceId = AuthConfig.current.appleServiceId,
            redirectUri = redirectUri,
            state = state,
            hashedNonce = hashedNonce,
        )
        val deferred = CompletableDeferred<Pair<String, String?>>()
        val server = embeddedServer(CIO, port = port) {
            routing {
                get("/callback") {
                    val token = call.request.queryParameters["token"] ?: error("no token")
                    val email = call.request.queryParameters["email"]?.takeIf { it.isNotBlank() }
                    deferred.complete(token to email)
                    call.respondText(
                        text = "<html><body>Signed in with Apple. You can close this tab.</body></html>",
                        contentType = ContentType.Text.Html,
                    )
                }
            }
        }.start(wait = false)
        try {
            withContext(Dispatchers.IO) {
                Desktop.getDesktop().browse(URI(authUrl))
            }
            val (customToken, email) = deferred.await()
            if (LogFlags.AUTH) println("[Auth] getAppleCredential: custom token received email=$email")
            Firebase.auth.signInWithCustomToken(customToken)
            if (LogFlags.AUTH) println("[Auth] getAppleCredential: signed in with custom token")
            pendingDirectSignInEmail = email
        } finally {
            server.stop(gracePeriodMillis = 100, timeoutMillis = 500)
        }
        null
    }.onFailure { if (LogFlags.AUTH) println("[Auth] getAppleCredential failed: $it") }.getOrNull()
}

@Serializable
private data class AppleState(
    val port: Int,
    val rawNonce: String,
)

private fun buildAppleState(
    port: Int,
    rawNonce: String,
): String {
    val json = Json.encodeToString(AppleState(port = port, rawNonce = rawNonce))
    return Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray())
}

private fun randomAppleBase64(bytes: Int): String {
    val buf = ByteArray(bytes)
    SecureRandom().nextBytes(buf)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf)
}

private fun encApple(s: String): String = URLEncoder.encode(s, "UTF-8")

private fun sha256Hex(input: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(input.toByteArray())
        .joinToString("") { "%02x".format(it) }

private fun buildAppleAuthUrl(
    serviceId: String,
    redirectUri: String,
    state: String,
    hashedNonce: String,
) = "https://appleid.apple.com/auth/authorize?" +
    "response_type=${encApple("code id_token")}&" +
    "response_mode=form_post&" +
    "client_id=${encApple(serviceId)}&" +
    "redirect_uri=${encApple(redirectUri)}&" +
    "scope=${encApple("name email")}&" +
    "nonce=${encApple(hashedNonce)}&" +
    "state=${encApple(state)}"
