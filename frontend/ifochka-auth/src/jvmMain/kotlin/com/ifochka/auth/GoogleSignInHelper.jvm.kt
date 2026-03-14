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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.SecureRandom
import java.util.Base64

private const val OAUTH_PORT = 8765

actual suspend fun getGoogleCredential(): AuthCredential? {
    if (AuthConfig.current.googleWebClientId.isEmpty()) return null
    return runCatching {
        val state = randomBase64(16)
        val nonce = randomBase64(16)
        val redirectUri = "http://localhost:$OAUTH_PORT/callback"
        val authUrl = buildGoogleAuthUrl(
            clientId = AuthConfig.current.googleWebClientId,
            redirectUri = redirectUri,
            state = state,
            nonce = nonce,
        )
        val deferred = CompletableDeferred<Pair<String, String?>>()
        val server = embeddedServer(CIO, port = OAUTH_PORT) {
            routing {
                get("/callback") {
                    call.respondText(
                        text = callbackHtml(redirectUri = redirectUri, state = state),
                        contentType = ContentType.Text.Html,
                    )
                }
                get("/callback/token") {
                    val idToken = call.request.queryParameters["id_token"] ?: error("no id_token")
                    val accessToken = call.request.queryParameters["access_token"]
                    deferred.complete(idToken to accessToken)
                    call.respondText("Signed in. You can close this tab.")
                }
            }
        }.start(wait = false)
        try {
            withContext(Dispatchers.IO) {
                Desktop.getDesktop().browse(URI(authUrl))
            }
            val (idToken) = deferred.await()
            if (LogFlags.AUTH) println("[Auth] getGoogleCredential: token received (idToken length=${idToken.length})")
            validateNonce(idToken = idToken, expectedNonce = nonce)
            if (LogFlags.AUTH) println("[Auth] getGoogleCredential: nonce ok, exchanging for custom token")
            val customToken = exchangeForCustomToken(idToken)
            if (customToken != null) {
                Firebase.auth.signInWithCustomToken(customToken)
                if (LogFlags.AUTH) println("[Auth] getGoogleCredential: signed in with custom token")
            } else {
                if (LogFlags.AUTH) println("[Auth] getGoogleCredential: custom token exchange failed")
            }
            null // sign-in handled via custom token — no credential needed
        } finally {
            server.stop(gracePeriodMillis = 100, timeoutMillis = 500)
        }
    }.onFailure { if (LogFlags.AUTH) println("[Auth] getGoogleCredential failed: $it") }.getOrNull()
}

@Serializable
private data class CustomTokenRequest(
    val googleIdToken: String,
)

@Serializable
private data class CustomTokenResponse(
    val customToken: String,
)

private suspend fun exchangeForCustomToken(googleIdToken: String): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val apiBaseUrl = AuthConfig.current.apiBaseUrl.trimEnd('/')
            if (apiBaseUrl.isEmpty()) {
                if (LogFlags.AUTH) println("[Auth] exchangeForCustomToken: apiBaseUrl not set")
                return@runCatching null
            }
            val requestBody = Json.encodeToString(CustomTokenRequest(googleIdToken))
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$apiBaseUrl/auth/custom-token"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()
            val response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                if (LogFlags.AUTH) println("[Auth] exchangeForCustomToken: HTTP ${response.statusCode()}")
                return@runCatching null
            }
            Json.decodeFromString<CustomTokenResponse>(response.body()).customToken
        }.getOrNull()
    }

private fun randomBase64(bytes: Int): String {
    val buf = ByteArray(bytes)
    SecureRandom().nextBytes(buf)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf)
}

private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

private fun buildGoogleAuthUrl(
    clientId: String,
    redirectUri: String,
    state: String,
    nonce: String,
) = "https://accounts.google.com/o/oauth2/v2/auth?" +
    "response_type=${enc("id_token token")}&" +
    "client_id=${enc(clientId)}&" +
    "redirect_uri=${enc(redirectUri)}&" +
    "scope=${enc("openid email profile")}&" +
    "nonce=${enc(nonce)}&" +
    "state=${enc(state)}"

private fun callbackHtml(
    redirectUri: String,
    state: String,
) = """
    <!DOCTYPE html><html><body><script>
      var params = new URLSearchParams(window.location.hash.slice(1));
      var idToken = params.get('id_token');
      var accessToken = params.get('access_token') || '';
      if (params.get('state') !== '$state' || !idToken) {
        document.body.innerText = 'Error: invalid state or missing token.';
      } else {
        window.location.href = '$redirectUri/token' +
          '?id_token=' + encodeURIComponent(idToken) +
          '&access_token=' + encodeURIComponent(accessToken);
      }
    </script><p>Signing in...</p></body></html>
    """.trimIndent()

private fun validateNonce(
    idToken: String,
    expectedNonce: String,
) {
    val payload = idToken.split(".").getOrNull(1) ?: run {
        if (LogFlags.AUTH) println("[Auth] validateNonce: no payload segment in JWT")
        return
    }
    val padded = payload.padEnd((payload.length + 3) / 4 * 4, '=')
    val json = String(Base64.getUrlDecoder().decode(padded))
    val actual = Json.parseToJsonElement(json).jsonObject["nonce"]?.jsonPrimitive?.content ?: run {
        if (LogFlags.AUTH) println("[Auth] validateNonce: no nonce claim in JWT payload")
        return
    }
    if (LogFlags.AUTH) {
        println(
            "[Auth] validateNonce: expected=$expectedNonce actual=$actual match=${actual == expectedNonce}",
        )
    }
    check(actual == expectedNonce) { "Nonce mismatch — possible token replay" }
}
