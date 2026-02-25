package com.ifochka.m14n.rest

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class ModularityTest {
    private val modules = ApplicationModules.of(M14nApplication::class.java)

    @Test
    fun documentModules() {
        Documenter(modules).writeDocumentation()
    }

    @Test
    fun verifyModules() {
        modules.verify()
    }
}
