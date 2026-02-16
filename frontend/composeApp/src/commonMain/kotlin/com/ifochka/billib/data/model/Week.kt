package com.ifochka.billib.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Week(
    val id: Long? = null,
    val date: String? = null,
)
