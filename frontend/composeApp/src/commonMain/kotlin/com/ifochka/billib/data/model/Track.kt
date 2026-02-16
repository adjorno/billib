package com.ifochka.billib.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: Long? = null,
    val title: String? = null,
    val artist: Artist? = null,
    val artistName: String? = null,
    val firstChartDate: String? = null,
    val peakGlobalRank: Int? = null,
    val totalWeeksOnChart: Int = 0,
)
