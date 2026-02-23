package com.ifochka.m14n.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DayTrack(
    val id: Long? = null,
    val track: Track? = null,
    val day: String? = null,
)
