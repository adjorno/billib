package com.ifochka.m14n.rest.integrity.domain

interface IntegrityService {
    fun verify(token: String, packageName: String): IntegrityVerdict
}