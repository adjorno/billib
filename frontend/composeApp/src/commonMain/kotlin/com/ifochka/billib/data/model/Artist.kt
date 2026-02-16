package com.ifochka.billib.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val id: Long? = null,
    val name: String? = null,
    val nameNormalized: String? = null,
)
