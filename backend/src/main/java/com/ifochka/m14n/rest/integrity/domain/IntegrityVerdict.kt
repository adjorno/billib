package com.ifochka.m14n.rest.integrity.domain

data class IntegrityVerdict(
    val pass: Boolean,
    val appRecognized: Boolean,
    val deviceIntegrity: List<String>,
    val licensingVerdict: String,
)
