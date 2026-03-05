package com.ifochka.m14n.rest.security

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class FirebaseJwtDecoder(
    firebaseApp: FirebaseApp,
) : JwtDecoder {
    private val auth = FirebaseAuth.getInstance(firebaseApp)

    override fun decode(token: String): Jwt {
        val firebaseToken = try {
            auth.verifyIdToken(token)
        } catch (e: FirebaseAuthException) {
            throw BadJwtException("Firebase token verification failed: ${e.authErrorCode}", e)
        }

        // Firebase claims contain iat/exp as Long (epoch seconds); Spring expects Instant.
        val claims = firebaseToken.claims.toMutableMap<String, Any>()
        val iat = (claims["iat"] as? Long)?.let { Instant.ofEpochSecond(it) } ?: Instant.now()
        val exp = (claims["exp"] as? Long)?.let { Instant.ofEpochSecond(it) } ?: iat.plusSeconds(3600)
        claims["iat"] = iat
        claims["exp"] = exp
        claims["sub"] = firebaseToken.uid

        return Jwt.withTokenValue(token)
            .header("alg", "RS256")
            .header("typ", "JWT")
            .claims { it.putAll(claims) }
            .build()
    }
}
