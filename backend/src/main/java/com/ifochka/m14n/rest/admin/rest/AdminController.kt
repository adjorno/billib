package com.ifochka.m14n.rest.admin.rest

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminController(
    firebaseApp: FirebaseApp,
) {
    private val auth = FirebaseAuth.getInstance(firebaseApp)

    @PostMapping("/admin/claims")
    fun setClaims(
        @RequestBody request: SetClaimsRequest,
    ): ResponseEntity<Unit> {
        val existing = auth.getUser(request.uid).customClaims.toMutableMap<String, Any>()
        if (request.admin != null) existing["admin"] = request.admin
        if (request.tier != null) existing["tier"] = request.tier
        auth.setCustomUserClaims(request.uid, existing)
        return ResponseEntity.noContent().build()
    }
}
