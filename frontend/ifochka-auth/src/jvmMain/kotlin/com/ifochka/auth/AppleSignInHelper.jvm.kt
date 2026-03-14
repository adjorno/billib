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
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

actual suspend fun getAppleCredential(): AuthCredential? {
    if (AuthConfig.current.appleServiceId.isEmpty()) return null
    return runCatching {
        val rawNonce = randomAppleBase64(32)
        val hashedNonce = sha256Hex(rawNonce)
        val state = randomAppleBase64(16)
        val port = ServerSocket(0).use { it.localPort }
        val redirectUri = "http://localhost:$port/callback"
        val authUrl = buildAppleAuthUrl(
            serviceId = AuthConfig.current.appleServiceId,
            redirectUri = redirectUri,
            state = state,
            hashedNonce = hashedNonce,
        )
        val deferred = CompletableDeferred<String>()
        val server = embeddedServer(CIO, port = port) {
            routing {
                get("/callback") {
                    call.respondText(
                        text = appleCallbackHtml(redirectUri = redirectUri, state = state),
                        contentType = ContentType.Text.Html,
                    )
                }
                get("/callback/token") {
                    val idToken = call.request.queryParameters["id_token"] ?: error("no id_token")
                    deferred.complete(idToken)
                    call.respondText("Signed in with Apple. You can close this tab.")
                }
            }
        }.start(wait = false)
        try {
            withContext(Dispatchers.IO) {
                Desktop.getDesktop().browse(URI(authUrl))
            }
            val idToken = deferred.await()
            if (LogFlags.AUTH) println("[Auth] getAppleCredential: token received (idToken length=${idToken.length})")
            if (LogFlags.AUTH) println("[Auth] getAppleCredential: nonce ok, exchanging for custom token")
            val customToken = exchangeAppleForCustomToken(
                appleIdToken = idToken,
                hashedNonce = hashedNonce,
            )
            if (customToken != null) {
                Firebase.auth.signInWithCustomToken(customToken)
                if (LogFlags.AUTH) println("[Auth] getAppleCredential: signed in with custom token")
            } else {
                if (LogFlags.AUTH) println("[Auth] getAppleCredential: custom token exchange failed")
            }
            null
        } finally {
            server.stop(gracePeriodMillis = 100, timeoutMillis = 500)
        }
    }.onFailure { if (LogFlags.AUTH) println("[Auth] getAppleCredential failed: $it") }.getOrNull()
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
    "response_mode=fragment&" +
    "client_id=${encApple(serviceId)}&" +
    "redirect_uri=${encApple(redirectUri)}&" +
    "scope=${encApple("name email")}&" +
    "nonce=${encApple(hashedNonce)}&" +
    "state=${encApple(state)}"

private fun appleCallbackHtml(
    redirectUri: String,
    state: String,
) = """
    <!DOCTYPE html><html><body><script>
      var params = new URLSearchParams(window.location.hash.slice(1));
      var idToken = params.get('id_token');
      if (params.get('state') !== '$state' || !idToken) {
        document.body.innerText = 'Error: invalid state or missing token.';
      } else {
        window.location.href = '$redirectUri/token' +
          '?id_token=' + encodeURIComponent(idToken);
      }
    </script><p>Signing in with Apple...</p></body></html>
    """.trimIndent()

@Serializable
private data class AppleCustomTokenRequest(
    val appleIdToken: String,
    val hashedNonce: String,
)

@Serializable
private data class AppleCustomTokenResponse(
    val customToken: String,
)

private suspend fun exchangeAppleForCustomToken(
    appleIdToken: String,
    hashedNonce: String,
): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val apiBaseUrl = AuthConfig.current.apiBaseUrl.trimEnd('/')
            if (apiBaseUrl.isEmpty()) return@runCatching null
            val requestBody = Json.encodeToString(AppleCustomTokenRequest(appleIdToken, hashedNonce))
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$apiBaseUrl/auth/custom-token"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()
            val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) return@runCatching null
            Json.decodeFromString<AppleCustomTokenResponse>(response.body()).customToken
        }.getOrNull()
    }
