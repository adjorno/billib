package com.ifochka.m14n.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Week(
    val id: Long? = null,
    val date: String? = null,
)
