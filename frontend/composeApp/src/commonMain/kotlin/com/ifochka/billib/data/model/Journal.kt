package com.ifochka.billib.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Journal(
    val id: Long? = null,
    val name: String? = null,
)
