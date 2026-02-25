package com.ifochka.m14n.rest.trends.rest

import com.ifochka.m14n.rest.chart.domain.ChartListRepository
import com.ifochka.m14n.rest.chart.domain.WeekRepository
import com.ifochka.m14n.rest.shared.M14n
import com.ifochka.m14n.rest.trends.domain.TrendTrackRepository
import com.ifochka.m14n.rest.trends.domain.TrendsService
import com.ifochka.m14n.rest.trends.rest.dtos.TrendList
import com.ifochka.m14n.rest.trends.rest.dtos.Trends
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TrendsController(
    private val mWeekRepository: WeekRepository,
    private val mChartListRepository: ChartListRepository,
    private val mTrendTrackRepository: TrendTrackRepository,
    private val mTrendsService: TrendsService,
) : ITrendsController {
    companion object {
        private const val LIST_SIZE_PER_TYPE = 10
    }

    @RequestMapping(value = ["/trends"], method = [RequestMethod.GET])
    fun getTrendsAPI(
        @RequestParam(required = false) @DateTimeFormat(pattern = M14n.CHART_DATE_FORMAT_STRING) date: String?,
    ): Trends {
        val theWeek = (
            if (date == null) {
                mChartListRepository.findLast(1L, PageRequest.of(0, 1)).content.firstOrNull()?.week
            } else {
                mWeekRepository.findByDate(M14n.CHART_DATE_FORMAT.format(date))
            }
        ) ?: throw IllegalStateException("Week not found for date: $date")
        val theTrendTracks = mTrendTrackRepository.findTrendsOfTheWeek(theWeek) ?: emptyList()
        val theTrendLists = mutableMapOf<Long, TrendList>()
        for (theTrendTrack in theTrendTracks) {
            val trendTypeId = theTrendTrack.type?.id ?: continue
            val trendType = theTrendTrack.type ?: continue
            val trendTypeLabel = trendType.description ?: trendType.name
            val track = theTrendTrack.track ?: continue
            val theTrendList = theTrendLists.getOrDefault(trendTypeId, TrendList(trendTypeLabel))
            if (theTrendList.tracks.size < LIST_SIZE_PER_TYPE) {
                theTrendList.tracks.add(track)
            }
            theTrendLists[trendTypeId] = theTrendList
        }
        return Trends(
            theWeek.date!!,
            theTrendLists.values.toTypedArray(),
        )
    }

    @RequestMapping(value = ["/generateTrends"], method = [RequestMethod.POST])
    fun generateTrendsAPI(
        @RequestParam() @DateTimeFormat(pattern = M14n.CHART_DATE_FORMAT_STRING) week: String,
        @RequestParam(required = false, defaultValue = "0") type: Int,
    ) {
        generateTrends(week, type.toLong())
    }

    override fun generateTrends(
        week: String,
        type: Long,
    ) {
        mTrendsService.generateTrends(week, type)
    }
}
