package com.m14n.billib.data.charttrack

import com.m14n.billib.data.chartlist.ChartList
import com.m14n.billib.data.dao.HasId
import com.m14n.billib.data.track.Track
import kotlinx.serialization.Serializable

@Serializable
data class ChartTrack(
    override val id: Long = 0,
    var chartList: ChartList? = null,
    val rank: Int,
    val track: Track
) : HasId {

    override fun toString() = "[id = $id] $chartList - $rank. $track"
}
