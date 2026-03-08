package com.ifochka.m14n.rest.admin.rest

data class SetClaimsRequest(
    val uid: String,
    val admin: Boolean? = null,
    val tier: String? = null,
)
