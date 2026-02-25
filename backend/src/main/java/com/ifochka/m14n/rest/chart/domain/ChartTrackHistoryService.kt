package com.ifochka.m14n.rest.chart.domain

import com.ifochka.m14n.rest.catalog.domain.TrackHistoryPort
import com.ifochka.m14n.rest.catalog.domain.TrackRepository
import com.ifochka.m14n.rest.shared.TrackNotFoundException
import org.springframework.stereotype.Service

@Service
class ChartTrackHistoryService(
    private val trackRepository: TrackRepository,
    private val chartRepository: ChartRepository,
    private val chartTrackRepository: ChartTrackRepository,
) : TrackHistoryPort {
    override fun getTrackHistory(
        trackId: Long,
        chartId: Long?,
    ): Map<String, Map<String, Int>> {
        val theTrack = trackRepository.findById(trackId).orElse(null)
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
