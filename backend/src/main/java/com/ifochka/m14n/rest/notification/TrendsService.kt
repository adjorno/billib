package com.ifochka.m14n.rest.notification

import com.ifochka.m14n.rest.TrackService
import com.ifochka.m14n.rest.chart.domain.ChartTrack
import com.ifochka.m14n.rest.chart.domain.ChartTrackRepository
import com.ifochka.m14n.rest.chart.domain.Week
import com.ifochka.m14n.rest.chart.domain.WeekRepository
import com.ifochka.m14n.rest.db.TrackRepository
import com.ifochka.m14n.rest.db.TrackUtils
import com.ifochka.m14n.rest.db.TrendTrack
import com.ifochka.m14n.rest.db.TrendTrackRepository
import com.ifochka.m14n.rest.db.TrendType
import com.ifochka.m14n.rest.db.TrendTypeRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TrendsService(
    private val trendTypeRepository: TrendTypeRepository,
    private val trackRepository: TrackRepository,
    private val weekRepository: WeekRepository,
    private val chartTrackRepository: ChartTrackRepository,
    private val trendTrackRepository: TrendTrackRepository,
    private val trackService: TrackService,
) {
    private val logger = LoggerFactory.getLogger(TrendsService::class.java)

    companion object {
        private const val DB_LIST_SIZE_PER_TYPE = 50

        private fun getOrCreateGainerCache(
            trackId: Long,
            cache: MutableMap<Long, Long>,
            trackService: TrackService,
            week: Week,
        ): Long {
            cache[trackId]?.let { return it }
            val theTrackHistory = trackService.getTrackHistory(trackId, 0L)
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
            return gainerValue
        }

        private fun filterByCharts(
            original: List<ChartTrack>,
            blacklisted: List<Long>,
        ): List<ChartTrack> = original.filter { !blacklisted.contains(it.chartList?.chart?.id) }

        private fun filterDebutsByCharts(
            original: List<Array<Any>>,
            blacklisted: List<Long>,
        ): List<Array<Any>> =
            original.filter { !blacklisted.contains((it.getOrNull(0) as? ChartTrack)?.chartList?.chart?.id) }

        // Blacklisted chart IDs: 13=Japan, 17=Gospel, 18=Christian
        private fun blacklistedCharts(): List<Long> = listOf(13L, 17L, 18L)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun generateTrends(
        week: String,
        type: Long,
    ) {
        logger.info("Started generating trends for week {}", week)
        val theWeek = weekRepository.findByDate(week) ?: return
        if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_GAINERS) generateGainers(theWeek)
        if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_DEBUTS) generateDebuts(theWeek)
        if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_FUTURES) generateFutures(theWeek)
        if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_SENIORS) generateSeniors(theWeek)
        logger.info("Finished generating trends for week {}", week)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private fun generateDebuts(week: Week) {
        logger.info("Generating debuts for week {}", week.date)
        val theBestDebuts = filterDebutsByCharts(
            chartTrackRepository.findDebuts(week.id!!),
            blacklistedCharts(),
        ).sortedByDescending { it[1] as? Int ?: 0 }
        val theDebutsType = trendTypeRepository.findById(TrendType.TYPE_DEBUTS).orElse(null)
        val theDebuts = mutableSetOf<Long>()
        for (obj in theBestDebuts) {
            val theChartTrack = obj.getOrNull(0) as? ChartTrack ?: continue
            val track = theChartTrack.track ?: continue
            val trackId = track.id ?: continue
            if (theDebuts.add(trackId)) {
                trendTrackRepository.save(TrendTrack(null, week, track, theDebutsType))
            }
        }
        logger.info("Generated {} debuts", theDebuts.size)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private fun generateFutures(week: Week) {
        logger.info("Generating futures for week {}", week.date)
        val theChartTracks = filterByCharts(
            chartTrackRepository.findByWeek(week),
            blacklistedCharts(),
        ).toMutableList()
        theChartTracks.sortWith(
            compareBy<ChartTrack> { if (it.lastWeekRank == 0) 1 else 0 }
                .thenBy { if (it.lastWeekRank != 0) it.rank - it.lastWeekRank else Int.MAX_VALUE }
                .thenBy { it.rank }
                .thenBy { it.chartList?.chart?.listSize ?: 0 },
        )
        val theFuturesType = trendTypeRepository.findById(TrendType.TYPE_FUTURES).orElse(null)
        val seenTracks = mutableSetOf<Long>()
        for (chartTrack in theChartTracks) {
            if (seenTracks.size >= DB_LIST_SIZE_PER_TYPE) break
            val trackId = chartTrack.track?.id ?: continue
            if (!seenTracks.add(trackId)) continue
            trendTrackRepository.save(TrendTrack(null, week, chartTrack.track, theFuturesType))
        }
        logger.info("Generated {} futures", seenTracks.size)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private fun generateSeniors(week: Week) {
        logger.info("Generating seniors for week {}", week.date)
        val theChartTracks = filterByCharts(chartTrackRepository.findByWeek(week), blacklistedCharts())
        val theTracks = trackRepository
            .sortByGlobalRank(TrackUtils.asTrackIds(TrackUtils.asTracks(theChartTracks)), DB_LIST_SIZE_PER_TYPE)
        val theSeniorsType = trendTypeRepository.findById(TrendType.TYPE_SENIORS).orElse(null)
        for (theTrack in theTracks) {
            trendTrackRepository.save(TrendTrack(null, week, theTrack, theSeniorsType))
        }
        logger.info("Generated {} seniors", theTracks.size)
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private fun generateGainers(week: Week) {
        logger.info("Generating gainers for week {}", week.date)
        val theGainersType = trendTypeRepository.findById(TrendType.TYPE_GAINERS).orElse(null)
        val theChartTracks = filterByCharts(chartTrackRepository.findByWeek(week), blacklistedCharts())
        val theTracks = TrackUtils.asTracks(theChartTracks)
        val theTrackIds = TrackUtils.asTrackIds(theTracks).toSet().toMutableList()
        logger.info("Unique tracks for gainers: {}", theTrackIds.size)
        val theGainerCache = mutableMapOf<Long, Long>()
        theTrackIds.sortByDescending { getOrCreateGainerCache(it, theGainerCache, trackService, week) }
        for (i in 0 until minOf(DB_LIST_SIZE_PER_TYPE, theTrackIds.size)) {
            val theTrackId = theTrackIds[i]
            trendTrackRepository
                .save(TrendTrack(null, week, TrackUtils.findTrack(theTracks, theTrackId), theGainersType))
        }
        logger.info("Generated {} gainers", minOf(DB_LIST_SIZE_PER_TYPE, theTrackIds.size))
    }
}
