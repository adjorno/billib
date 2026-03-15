package com.ifochka.m14n.rest.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

@RestController
@RequestMapping("/auth")
class AuthController(
    firebaseApp: FirebaseApp,
    @Value("\${firebase.web-api-key}") private val webApiKey: String,
) {
    private val objectMapper = ObjectMapper()
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    private val auth = FirebaseAuth.getInstance(firebaseApp)
    private val restClient = RestClient.create()

    @PostMapping("/custom-token")
    fun customToken(
        @RequestBody body: CustomTokenRequest,
    ): ResponseEntity<CustomTokenResponse> {
        val uid = resolveFirebaseUid(body.googleIdToken)
            ?: run {
                logger.warn("[CustomToken] No valid ID token provided")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }
        val customToken = auth.createCustomToken(uid)
        logger.info("[CustomToken] Issued for uid=$uid")
        return ResponseEntity.ok(CustomTokenResponse(customToken = customToken))
    }

    @PostMapping("/apple/callback", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun appleCallback(
        @RequestParam("id_token") idToken: String,
        @RequestParam("state") state: String,
    ): ResponseEntity<Unit> {
        val callbackState = runCatching {
            val node = objectMapper.readTree(String(Base64.getUrlDecoder().decode(state)))
            AppleCallbackState(
                port = node.get("port").asInt(),
                rawNonce = node.get("rawNonce").asText(),
            )
        }.getOrNull() ?: run {
            logger.warn("[AppleCallback] Failed to parse state")
            return ResponseEntity.badRequest().build()
        }
        val uid = resolveFirebaseUidFromApple(
            appleIdToken = idToken,
            rawNonce = callbackState.rawNonce,
        ) ?: run {
            logger.warn("[AppleCallback] Failed to resolve Firebase UID")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val email = resolveAppleEmailByUid(uid)
        val customToken = auth.createCustomToken(uid)
        logger.info("[AppleCallback] Issued custom token for uid=$uid email=$email")
        val encodedToken = URLEncoder.encode(customToken, StandardCharsets.UTF_8)
        val encodedEmail = URLEncoder.encode(email ?: "", StandardCharsets.UTF_8)
        val redirectUrl = "http://localhost:${callbackState.port}/callback?token=$encodedToken&email=$encodedEmail"
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, redirectUrl)
            .build()
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

    private fun resolveAppleEmailByUid(uid: String): String? =
        runCatching { auth.getUser(uid)?.email }
            .onFailure { logger.warn("[AppleCallback] Failed to fetch email for uid=$uid: ${it.message}") }
            .getOrNull()

    private fun resolveFirebaseUidFromApple(
        appleIdToken: String,
        rawNonce: String,
    ): String? {
        val encodedToken = URLEncoder.encode(appleIdToken, StandardCharsets.UTF_8)
        val postBody = "id_token=$encodedToken&providerId=apple.com"
        return runCatching {
            restClient.post()
                .uri("https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$webApiKey")
                .contentType(MediaType.APPLICATION_JSON)
                .body(SignInWithIdpRequest(postBody = postBody, nonce = rawNonce))
                .retrieve()
                .body(SignInWithIdpResponse::class.java)
                ?.localId
        }.onFailure { logger.warn("[AppleCallback] Apple token exchange failed: ${it.message}") }.getOrNull()
    }

    data class CustomTokenRequest(
        val googleIdToken: String = "",
    )

    data class CustomTokenResponse(
        val customToken: String,
    )

    private data class AppleCallbackState(
        val port: Int = 0,
        val rawNonce: String = "",
    )

    private data class SignInWithIdpRequest(
        val requestUri: String = "http://localhost",
        val postBody: String,
        val returnSecureToken: Boolean = true,
        val nonce: String? = null,
    )

    private data class SignInWithIdpResponse(
        val localId: String = "",
    )
}
