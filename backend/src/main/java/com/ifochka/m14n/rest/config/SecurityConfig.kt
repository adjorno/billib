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
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @param:Value("\${firebase.web-api-key}") private val apiKey: String,
    private val jwtDecoder: JwtDecoder,
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
                "/auth/google/custom-token",
                "/auth/apple/custom-token",
                "/auth/apple/callback",
            )
            .cors { it.configurationSource(corsSource()) }
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
        return http.build()
    }

    // All other paths: require a valid Firebase JWT verified by Firebase Admin SDK.
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
                    // User sync is available to any authenticated user (anonymous or registered).
                    .requestMatchers(HttpMethod.POST, "/user/sync").hasRole("USER")
                    // All other POST endpoints in this API are intentionally admin-only (data mutation).
                    // Read-only (GET) endpoints are accessible to any authenticated user (ROLE_USER).
                    .requestMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
                    .requestMatchers("/duplicate/artist", "/duplicate/track").hasRole("ADMIN")
                    .anyRequest().hasRole("USER")
            }
            .addFilterBefore(ApiKeyAuthFilter(apiKey), BearerTokenAuthenticationFilter::class.java)
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.decoder(jwtDecoder)
                    jwt.jwtAuthenticationConverter(
                        JwtAuthenticationConverter().apply {
                            setJwtGrantedAuthoritiesConverter(FirebaseJwtGrantedAuthoritiesConverter())
                        },
                    )
                }
            }
        return http.build()
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
