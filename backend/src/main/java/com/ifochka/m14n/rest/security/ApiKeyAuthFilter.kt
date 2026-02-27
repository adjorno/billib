package com.ifochka.m14n.rest.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Collections

class ApiKeyAuthFilter(
    private val apiKey: String,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.drop("Bearer ".length)

        if (token != null && token == apiKey) {
            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
                "app-client",
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER")),
            )
            // Hide Authorization header so BearerTokenAuthenticationFilter
            // does not attempt to decode the API key as a JWT.
            filterChain.doFilter(HiddenAuthorizationRequestWrapper(request), response)
            return
        }
        filterChain.doFilter(request, response)
    }

    private class HiddenAuthorizationRequestWrapper(
        request: HttpServletRequest,
    ) : HttpServletRequestWrapper(request) {
        override fun getHeader(name: String): String? =
            if (name.equals("Authorization", ignoreCase = true)) null else super.getHeader(name)

        override fun getHeaders(name: String): java.util.Enumeration<String> =
            if (name.equals("Authorization", ignoreCase = true)) {
                Collections.emptyEnumeration()
            } else {
                super.getHeaders(name)
            }
    }
}
