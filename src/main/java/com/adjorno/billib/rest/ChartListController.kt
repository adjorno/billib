package com.adjorno.billib.rest

import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.data.domain.PageRequest
import com.adjorno.billib.rest.db.*
import com.m14n.billib.data.BB
import org.springframework.data.repository.findByIdOrNull

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
    fun getChartListById(@RequestParam(name = "id") chartListId: Long): ChartList {
        val theChartList =
            chartListRepository.findByIdOrNull(chartListId) ?: throw ChartListNotFoundException()
        return getPrintableChartList(theChartList)
    }

    @RequestMapping(value = ["/chartList/getByDate"], method = [RequestMethod.GET])
    fun getChartListByDate(
        @RequestParam(name = "chart_id") chartId: Long,
        @RequestParam(required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) date: String?
    ): ChartList {
        val theChart = chartRepository.findByIdOrNull(chartId) ?: throw ChartListNotFoundException()
        var theChartList: ChartList? = null
        if (date.isNullOrBlank()) {
            theChartList = chartListRepository.findLast(theChart, PageRequest.of(0, 1)).content[0]
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
    fun getFirstAppearance(@RequestParam(name = "track_id") trackId: Long): ChartList? {
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