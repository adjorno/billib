package com.ifochka.m14n.rest.user.rest

import com.ifochka.m14n.rest.user.domain.UserProfile
import com.ifochka.m14n.rest.user.domain.UserProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class UserController(
    private val repository: UserProfileRepository,
) {
    private val log = LoggerFactory.getLogger(UserController::class.java)

    companion object {
        private const val ANONYMOUS_PROVIDER = "anonymous"
    }

    @PostMapping("/user/sync")
    fun sync(authentication: Authentication): ResponseEntity<UserProfile> {
        val jwt = authentication.asJwt() ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val uid = jwt.name

        @Suppress("UNCHECKED_CAST")
        val firebaseClaim = jwt.tokenAttributes["firebase"] as? Map<String, Any>
        val provider = firebaseClaim?.get("sign_in_provider") as? String
        val isAnon = provider == ANONYMOUS_PROVIDER
        val email = jwt.tokenAttributes["email"] as? String
        val displayName = jwt.tokenAttributes["name"] as? String

        val isNew = !repository.existsById(uid)
        log.info(
            "[UserSync] uid={} provider={} isAnon={} email={} displayName={} new={}",
            uid,
            provider,
            isAnon,
            email,
            displayName,
            isNew,
        )

        val existing = repository.findById(uid).orElse(UserProfile(firebaseUid = uid))
        val saved = repository.save(
            existing.copy(
                isAnonymous = isAnon,
                email = email ?: existing.email,
                displayName = displayName ?: existing.displayName,
                lastLoginAt = Instant.now(),
            ),
        )
        log.info("[UserSync] saved uid={} isAnon={} email={}", saved.firebaseUid, saved.isAnonymous, saved.email)
        return ResponseEntity.ok(saved)
    }

    @GetMapping("/user/me")
    fun me(authentication: Authentication): ResponseEntity<UserProfile> {
        val jwt = authentication.asJwt() ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val uid = jwt.name
        return repository.findById(uid)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    private fun Authentication.asJwt(): JwtAuthenticationToken? = this as? JwtAuthenticationToken
}
