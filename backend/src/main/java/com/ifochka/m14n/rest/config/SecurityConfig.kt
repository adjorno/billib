package com.ifochka.m14n.rest.config

import com.ifochka.m14n.rest.security.ApiKeyAuthFilter
import com.ifochka.m14n.rest.security.FirebaseJwtGrantedAuthoritiesConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @param:Value("\${firebase.project-id}") private val projectId: String,
    @param:Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") private val jwksUri: String,
    @param:Value("\${api.key}") private val apiKey: String,
) {
    // Public paths: no OAuth2 filter — BearerTokenAuthenticationFilter never runs here.
    @Bean
    @Order(1)
    fun publicFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/dev/token",
            )
            .cors { it.configurationSource(corsSource()) }
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
        return http.build()
    }

    // All other paths: require a valid Firebase JWT.
    @Bean
    @Order(2)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/admin/claims").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
                    .requestMatchers("/duplicate/artist", "/duplicate/track").hasRole("ADMIN")
                    .anyRequest().hasRole("USER")
            }
            .addFilterBefore(ApiKeyAuthFilter(apiKey), BearerTokenAuthenticationFilter::class.java)
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.decoder(firebaseJwtDecoder())
                    jwt.jwtAuthenticationConverter(
                        JwtAuthenticationConverter().apply {
                            setJwtGrantedAuthoritiesConverter(FirebaseJwtGrantedAuthoritiesConverter())
                        },
                    )
                }
            }
        return http.build()
    }

    @Bean
    fun firebaseJwtDecoder(): JwtDecoder {
        val decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build()
        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtTimestampValidator(),
                JwtIssuerValidator("https://securetoken.google.com/$projectId"),
                JwtClaimValidator<Any>("aud") { aud ->
                    aud?.toString()?.contains(projectId) == true
                },
            ),
        )
        return decoder
    }

    private fun corsSource() =
        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration(
                "/**",
                CorsConfiguration().apply {
                    addAllowedOriginPattern("*")
                    addAllowedHeader("*")
                    addAllowedMethod("*")
                },
            )
        }
}
