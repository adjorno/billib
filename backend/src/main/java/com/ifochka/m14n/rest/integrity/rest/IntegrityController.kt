package com.ifochka.m14n.rest.integrity.rest

import com.ifochka.m14n.rest.integrity.domain.IntegrityService
import com.ifochka.m14n.rest.integrity.domain.IntegrityVerdict
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/integrity")
class IntegrityController(
    private val integrityService: IntegrityService,
) {
    @PostMapping("/verify")
    fun verify(
        @RequestBody body: IntegrityRequest,
    ): ResponseEntity<IntegrityVerdict> {
        if (body.integrityToken.isBlank() || body.packageName.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        val verdict = integrityService.verify(
            token = body.integrityToken,
            packageName = body.packageName,
        )
        return ResponseEntity.ok(verdict)
    }

    data class IntegrityRequest(
        val integrityToken: String = "",
        val packageName: String = "",
    )
}