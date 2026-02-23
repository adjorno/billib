package com.ifochka.m14n.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TrendList(
    val name: String? = null,
    val tracks: List<Track>? = null,
)
