package com.ifochka.billib.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChartList(
    val id: Long? = null,
    val chart: Chart? = null,
    val week: Week? = null,
    val chartTracks: List<ChartTrack>? = null,
)
