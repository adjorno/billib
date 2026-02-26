package com.ifochka.m14n.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TrackInfo(
    val track: Track,
    val history: Map<String, Map<String, Int>>? = null,
    val globalRank: Long = 0,
)
