package com.ifochka.m14n.rest.dev

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient

@Tag(name = "Dev", description = "Local development helpers — not available in production")
@RestController
@Profile("local")
@RequestMapping("/dev")
class DevTokenController(
    @param:Value("\${firebase.web-api-key}") private val webApiKey: String,
) {
    private val restClient = RestClient.create()
    private val signUpUrl = System.getenv("FIREBASE_AUTH_EMULATOR_HOST")
        ?.let { "http://$it/identitytoolkit.googleapis.com/v1/accounts:signUp?key=$webApiKey" }
        ?: "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$webApiKey"

    @Operation(summary = "Get anonymous Firebase ID token (local only)")
    @PostMapping("/token", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getToken(): ResponseEntity<Map<String, String>> {
        val response = restClient.post()
            .uri(signUpUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"returnSecureToken":true}""")
            .retrieve()
            .body(FirebaseSignUpResponse::class.java)
            ?: return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(mapOf("error" to "Empty response from Firebase"))

        return ResponseEntity.ok(
            mapOf(
                "token" to response.idToken,
                "expiresIn" to response.expiresIn,
                "hint" to "Paste token value into Swagger Authorize → Bearer",
            ),
        )
    }

    private data class FirebaseSignUpResponse(
        val idToken: String = "",
        val expiresIn: String = "",
    )
}
