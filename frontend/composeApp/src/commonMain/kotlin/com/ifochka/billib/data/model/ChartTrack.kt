package com.ifochka.billib.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChartTrack(
    val track: Track? = null,
    val rank: Int = 0,
    val lastWeekRank: Int = 0,
) {
    val rankChange: Int
        get() = if (lastWeekRank == 0) 0 else lastWeekRank - rank

    val isDebut: Boolean
        get() = lastWeekRank == 0

    val isUp: Boolean
        get() = rankChange > 0

    val isDown: Boolean
        get() = rankChange < 0
}
