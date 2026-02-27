package com.ifochka.m14n.rest.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class FirebaseJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>(SimpleGrantedAuthority("ROLE_USER"))
        if (jwt.getClaim<Boolean>("admin") == true) {
            authorities += SimpleGrantedAuthority("ROLE_ADMIN")
        }
        if (jwt.getClaim<String>("tier") == "premium") {
            authorities += SimpleGrantedAuthority("ROLE_PREMIUM")
        }
        return authorities
    }
}
