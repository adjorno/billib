package com.ifochka.m14n.rest.chart.rest

import com.ifochka.m14n.rest.catalog.domain.TrackRepository
import com.ifochka.m14n.rest.chart.domain.ChartList
import com.ifochka.m14n.rest.chart.domain.ChartListRepository
import com.ifochka.m14n.rest.chart.domain.ChartRepository
import com.ifochka.m14n.rest.chart.domain.ChartTrackRepository
import com.ifochka.m14n.rest.chart.domain.WeekRepository
import com.ifochka.m14n.rest.shared.ChartListNotFoundException
import com.ifochka.m14n.rest.shared.M14n
import com.ifochka.m14n.rest.shared.TrackNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChartListController {
    @Autowired
    private lateinit var chartListRepository: ChartListRepository

    @Autowired
    private lateinit var chartTrackRepository: ChartTrackRepository

    @Autowired
    private lateinit var weekRepository: WeekRepository

    @Autowired
    private lateinit var chartRepository: ChartRepository

    @Autowired
    private lateinit var trackRepository: TrackRepository

    @Autowired
    private lateinit var chartTrackController: ChartTrackController

    @RequestMapping(value = ["/chartList/getById"], method = [RequestMethod.GET])
    fun getChartListById(
        @RequestParam(name = "id") chartListId: Long,
    ): ChartList {
        val theChartList =
            chartListRepository.findByIdOrNull(chartListId) ?: throw ChartListNotFoundException()
        return getPrintableChartList(theChartList)
    }

    @RequestMapping(value = ["/chartList/getByDate"], method = [RequestMethod.GET])
    fun getChartListByDate(
        @RequestParam(name = "chart_id") chartId: Long,
        @RequestParam(required = false) @DateTimeFormat(pattern = M14n.CHART_DATE_FORMAT_STRING) date: String?,
    ): ChartList {
        val theChart = chartRepository.findByIdOrNull(chartId) ?: throw ChartListNotFoundException()
        var theChartList: ChartList? = null
        if (date.isNullOrBlank()) {
            theChartList = chartListRepository.findLast(theChart, PageRequest.of(0, 1)).content.firstOrNull()
        } else {
            val theWeeks = weekRepository.findClosest(date)
            if (!theWeeks.isNullOrEmpty()) {
                theChartList = chartListRepository.findByChartAndWeek(theChart, theWeeks[0])
            }
        }
        if (theChartList == null) {
            throw ChartListNotFoundException()
        }
        return getPrintableChartList(theChartList)
    }

    @RequestMapping(value = ["/chartList/getFirstAppearance"], method = [RequestMethod.GET])
    fun getFirstAppearance(
        @RequestParam(name = "track_id") trackId: Long,
    ): ChartList? {
        val theTrack = trackRepository.findByIdOrNull(trackId) ?: throw TrackNotFoundException()
        val theDebut = chartTrackController.getDebut(theTrack)
        val theChartList = theDebut.chartList
        theChartList?.chartTracks = listOf(theDebut)
        return theChartList
    }

    private fun getPrintableChartList(chartList: ChartList): ChartList {
        val theChartTracks = chartTrackRepository.findByChartList(chartList)
        chartList.chartTracks = theChartTracks
        return chartList
    }
}
