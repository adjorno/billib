package com.ifochka.m14n.rest.user.rest

import com.ifochka.m14n.rest.user.domain.UserProfile
import com.ifochka.m14n.rest.user.domain.UserProfileRepository
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
    companion object {
        private const val ANONYMOUS_PROVIDER = "anonymous"
    }

    @PostMapping("/user/sync")
    fun sync(authentication: Authentication): ResponseEntity<UserProfile> {
        if (authentication !is JwtAuthenticationToken) return ResponseEntity.status(401).build()
        val uid = authentication.name

        @Suppress("UNCHECKED_CAST")
        val firebaseClaim = authentication.tokenAttributes["firebase"] as? Map<String, Any>
        val isAnon = firebaseClaim?.get("sign_in_provider") == ANONYMOUS_PROVIDER
        val email = authentication.tokenAttributes["email"] as? String
        val displayName = authentication.tokenAttributes["name"] as? String
        val existing = repository.findById(uid).orElse(UserProfile(firebaseUid = uid))
        return ResponseEntity.ok(
            repository.save(
                existing.copy(
                    isAnonymous = isAnon,
                    email = email ?: existing.email,
                    displayName = displayName ?: existing.displayName,
                    lastLoginAt = Instant.now(),
                ),
            ),
        )
    }

    @GetMapping("/user/me")
    fun me(authentication: Authentication): ResponseEntity<UserProfile> {
        if (authentication !is JwtAuthenticationToken) return ResponseEntity.status(401).build()
        val uid = authentication.name
        return repository.findById(uid)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }
}
