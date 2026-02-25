package com.ifochka.m14n.rest

import com.ifochka.m14n.rest.chart.domain.ChartRepository
import com.ifochka.m14n.rest.chart.domain.ChartTrackRepository
import com.ifochka.m14n.rest.db.TrackRepository
import com.ifochka.m14n.rest.shared.TrackNotFoundException
import org.springframework.stereotype.Service

@Service
class TrackService(
    private val trackRepository: TrackRepository,
    private val chartRepository: ChartRepository,
    private val chartTrackRepository: ChartTrackRepository,
) {
    fun getTrackHistory(
        id: Long,
        chartId: Long?,
    ): Map<String, Map<String, Int>> {
        val theTrack = trackRepository.findById(id).orElse(null)
            ?: throw TrackNotFoundException()
        val theRequestedChart = chartId?.let {
            if (it > 0L) chartRepository.findById(it).orElse(null) else null
        }
        val theCharts = if (theRequestedChart == null) chartRepository.findAll() else listOf(theRequestedChart)
        val theFullHistory = mutableMapOf<String, MutableMap<String, Int>>()
        val theChartTracks = chartTrackRepository.findByTrackInCharts(theTrack, theCharts)
        for (theChartTrack in theChartTracks) {
            val theChartName = theChartTrack.chartList?.chart?.name ?: continue
            val weekDate = theChartTrack.chartList?.week?.date ?: continue
            val theChartHistory = theFullHistory.getOrPut(theChartName) { sortedMapOf() }
            theChartHistory[weekDate] = theChartTrack.rank
        }
        return theFullHistory
    }
}
