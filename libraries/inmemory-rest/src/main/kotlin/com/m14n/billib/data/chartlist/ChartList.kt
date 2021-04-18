package com.m14n.billib.data.chartlist

import com.m14n.billib.data.chart.Chart
import com.m14n.billib.data.charttrack.ChartTrack
import com.m14n.billib.data.dao.HasId
import com.m14n.billib.data.week.Week
import kotlinx.serialization.Serializable

@Serializable
data class ChartList(
    override val id: Long,
    val chart: Chart,
    val week: Week,
    var chartTracks: List<ChartTrack>? = null
) : HasId {

    override fun toString() = "ChartList $chart $week"
}
