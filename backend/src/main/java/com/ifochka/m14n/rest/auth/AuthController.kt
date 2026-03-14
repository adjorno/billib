package com.ifochka.m14n.rest.auth

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/auth")
class AuthController(
    firebaseApp: FirebaseApp,
    @Value("\${firebase.web-api-key}") private val webApiKey: String,
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    private val auth = FirebaseAuth.getInstance(firebaseApp)
    private val restClient = RestClient.create()

    @PostMapping("/custom-token")
    fun customToken(
        @RequestBody body: CustomTokenRequest,
    ): ResponseEntity<CustomTokenResponse> {
        val uid = resolveFirebaseUid(body.googleIdToken)
            ?: run {
                logger.warn("[CustomToken] Failed to resolve Firebase UID from Google ID token")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }
        val customToken = auth.createCustomToken(uid)
        logger.info("[CustomToken] Issued for uid=$uid")
        return ResponseEntity.ok(CustomTokenResponse(customToken = customToken))
    }

    private fun resolveFirebaseUid(googleIdToken: String): String? {
        val encodedToken = URLEncoder.encode(googleIdToken, StandardCharsets.UTF_8)
        val postBody = "id_token=$encodedToken&providerId=google.com"
        return runCatching {
            restClient.post()
                .uri("https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$webApiKey")
                .contentType(MediaType.APPLICATION_JSON)
                .body(SignInWithIdpRequest(postBody = postBody))
                .retrieve()
                .body(SignInWithIdpResponse::class.java)
                ?.localId
        }.onFailure { logger.warn("[CustomToken] Google token exchange failed: ${it.message}") }.getOrNull()
    }

    data class CustomTokenRequest(
        val googleIdToken: String = "",
    )

    data class CustomTokenResponse(
        val customToken: String,
    )

    private data class SignInWithIdpRequest(
        val requestUri: String = "http://localhost",
        val postBody: String,
        val returnSecureToken: Boolean = true,
    )

    private data class SignInWithIdpResponse(
        val localId: String = "",
    )
}
