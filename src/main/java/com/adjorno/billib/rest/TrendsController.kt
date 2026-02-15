package com.adjorno.billib.rest

import com.adjorno.billib.rest.db.*
import com.adjorno.billib.rest.model.TrendList
import com.adjorno.billib.rest.model.Trends
import com.m14n.billib.data.BB
import com.m14n.ex.Ex
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TrendsController(
    private val mTrendTypeRepository: TrendTypeRepository,
    private val mTrackRepository: TrackRepository,
    private val mWeekRepository: WeekRepository,
    private val mChartTrackRepository: ChartTrackRepository,
    private val mChartListRepository: ChartListRepository,
    private val mTrendTrackRepository: TrendTrackRepository,
    private val mTrackController: TrackController
) : ITrendsController {

    companion object {
        private const val LIST_SIZE_PER_TYPE = 10
        private const val DB_LIST_SIZE_PER_TYPE = 5 * LIST_SIZE_PER_TYPE

        private fun getOrCreateGainerCache(
            trackId: Long, cache: MutableMap<Long, Long>, trackController: TrackController,
            week: Week
        ): Long {
            if (cache.containsKey(trackId)) {
                return cache[trackId]!!
            } else {
                val theTrackHistory = trackController.getTrackHistory(trackId, 0L)
                var gainerValue = 1L
                for (chartHistory in theTrackHistory.values) {
                    val theSortedHistory = chartHistory.entries.sortedByDescending { it.key }
                    if (theSortedHistory[0].key == week.date) {
                        for (i in 1 until theSortedHistory.size) {
                            val theValue = theSortedHistory[i].value
                            if ((theValue == 1) || (theValue < theSortedHistory[i - 1].value)) {
                                break
                            }
                            if (i + 1 > gainerValue) {
                                gainerValue = (i + 1).toLong()
                            }
                        }
                    }
                }
                cache[trackId] = gainerValue
                println("CACHE ${cache.size}: $trackId $gainerValue")
                return gainerValue
            }
        }

        private fun filterByCharts(original: List<ChartTrack>, blacklisted: List<Long>): List<ChartTrack> {
            val result = mutableListOf<ChartTrack>()
            for (chartTrack in original) {
                if (!blacklisted.contains(chartTrack.chartList?.chart?.id)) {
                    result.add(chartTrack)
                }
            }
            return result
        }

        private fun filterDebutsByCharts(original: List<Array<Any>>, blacklisted: List<Long>): List<Array<Any>> {
            val result = mutableListOf<Array<Any>>()
            for (chartTrack in original) {
                if (!blacklisted.contains((chartTrack[0] as ChartTrack).chartList?.chart?.id)) {
                    result.add(chartTrack)
                }
            }
            return result
        }

        private fun blacklistedCharts(): List<Long> {
            return listOf(13L /*Japan*/, 17L /*Gospel*/, 18L /*Christian*/)
        }
    }

    @RequestMapping(value = ["/trends"], method = [RequestMethod.GET])
    fun getTrendsAPI(
        @RequestParam(required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) date: String?
    ): Trends {
        val theWeek = (
            if (date == null) mChartListRepository.findLast(1L, PageRequest.of(0, 1)).content.firstOrNull()?.week
            else mWeekRepository.findByDate(BB.CHART_DATE_FORMAT.format(date))
        ) ?: throw IllegalStateException("Week not found for date: $date")
        val theTrendTracks = mTrendTrackRepository.findTrendsOfTheWeek(theWeek) ?: emptyList()
        val theTrendLists = mutableMapOf<Long, TrendList>()
        for (theTrendTrack in theTrendTracks) {
            val trendTypeId = theTrendTrack.type?.id ?: continue
            val trendType = theTrendTrack.type ?: continue
            // Use description if available, otherwise fall back to name
            val trendTypeLabel = trendType.description ?: trendType.name
            val track = theTrendTrack.track ?: continue
            val theTrendList =
                theTrendLists.getOrDefault(trendTypeId, TrendList(trendTypeLabel))
            if (theTrendList.tracks.size < LIST_SIZE_PER_TYPE) {
                theTrendList.tracks.add(track)
            }
            theTrendLists[trendTypeId] = theTrendList
        }
        val theTrends = Trends(
            theWeek.date!!,
            theTrendLists.values.toTypedArray()
        )
        return theTrends
    }

    @RequestMapping(value = ["/generateTrends"], method = [RequestMethod.POST])
    fun generateTrendsAPI(
        @RequestParam() @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) week: String,
        @RequestParam(required = false, defaultValue = "0") type: Int
    ) {
        generateTrends(week, type.toLong())
    }

    @Transactional(propagation = Propagation.REQUIRED)
    override fun generateTrends(week: String, type: Long) {
        println("STARTED GENERATING TRENDS $week")
        val theWeek = mWeekRepository.findByDate(week)
        if (theWeek != null) {
            if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_GAINERS) {
                generateGainers(theWeek)
            }
            if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_DEBUTS) {
                generateDebuts(theWeek)
            }
            if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_FUTURES) {
                generateFutures(theWeek)
            }
            if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_SENIORS) {
                generateSeniors(theWeek)
            }
        }
        println("FINISHED GENERATING TRENDS")
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private fun generateDebuts(week: Week) {
        println("STARTED GENERATE DEBUTS")
        val theBestDebuts = filterDebutsByCharts(
            mChartTrackRepository.findDebuts(week.id!!),
            blacklistedCharts()
        ).sortedByDescending { it[1] as? Int ?: 0 }
        val theDebutsType = mTrendTypeRepository.findById(TrendType.TYPE_DEBUTS).orElse(null)
        val theDebuts = mutableSetOf<Long>()
        for (obj in theBestDebuts) {
            val theChartTrack = obj[0] as ChartTrack
            val track = theChartTrack.track ?: continue
            val trackId = track.id ?: continue
            if (!theDebuts.contains(trackId)) {
                mTrendTrackRepository.save(TrendTrack(null, week, track, theDebutsType))
                theDebuts.add(trackId)
            }
        }
        println("FINISHED GENERATE DEBUTS")
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private fun generateFutures(week: Week) {
        println("STARTED GENERATE FUTURES")
        val theChartTracks = filterByCharts(
            mChartTrackRepository.findByWeek(week),
            blacklistedCharts()
        ).toMutableList()
        theChartTracks.sortWith(
            compareBy<ChartTrack> { if (it.lastWeekRank == 0) 1 else 0 } // non-debuts first
                .thenBy { if (it.lastWeekRank != 0) it.rank - it.lastWeekRank else Int.MAX_VALUE }
                .thenBy { it.rank }
                .thenBy { it.chartList?.chart?.listSize ?: 0 }
        )
        val theFuturesType = mTrendTypeRepository.findById(TrendType.TYPE_FUTURES).orElse(null)
        for (i in 0 until minOf(theChartTracks.size, DB_LIST_SIZE_PER_TYPE)) {
            mTrendTrackRepository.save(TrendTrack(null, week, theChartTracks[i].track, theFuturesType))
        }
        println("FINISHED GENERATE FUTURES")
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private fun generateSeniors(week: Week) {
        println("STARTED GENERATE SENIORS")
        val theChartTracks = filterByCharts(
            mChartTrackRepository.findByWeek(week),
            blacklistedCharts()
        )
        val theTracks = mTrackRepository
            .sortByGlobalRank(TrackUtils.asTrackIds(TrackUtils.asTracks(theChartTracks)), DB_LIST_SIZE_PER_TYPE)
        val theSeniorsType = mTrendTypeRepository.findById(TrendType.TYPE_SENIORS).orElse(null)
        for (theTrack in theTracks) {
            mTrendTrackRepository.save(TrendTrack(null, week, theTrack, theSeniorsType))
        }
        println("FINISHED GENERATE SENIORS")
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private fun generateGainers(week: Week) {
        println("STARTED GENERATE GAINERS")
        val theGainersType = mTrendTypeRepository.findById(TrendType.TYPE_GAINERS).orElse(null)
        val theChartTracks = filterByCharts(
            mChartTrackRepository.findByWeek(week),
            blacklistedCharts()
        )
        val theTracks = TrackUtils.asTracks(theChartTracks)
        val theTrackIds = TrackUtils.asTrackIds(theTracks).toSet().toMutableList()
        println("UNIQUE TRACKS ${theTrackIds.size}")
        val theGainerCache = mutableMapOf<Long, Long>()
        theTrackIds.sortByDescending {
            getOrCreateGainerCache(it, theGainerCache, mTrackController, week)
        }
        for (i in 0 until minOf(DB_LIST_SIZE_PER_TYPE, theTrackIds.size)) {
            val theTrackId = theTrackIds[i]
            mTrendTrackRepository
                .save(TrendTrack(null, week, TrackUtils.findTrack(theTracks, theTrackId), theGainersType))
        }
        println("FINISHED GENERATE GAINERS")
    }
}
