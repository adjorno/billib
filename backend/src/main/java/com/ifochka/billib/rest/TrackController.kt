package com.ifochka.billib.rest

import com.ifochka.billib.rest.db.*
import com.ifochka.billib.rest.model.TrackInfo
import com.m14n.ex.Ex
import com.m14n.billib.data.BB
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import jakarta.persistence.EntityManager

@RestController
class TrackController(
    private val mChartRepository: ChartRepository,
    private val mTrackRepository: TrackRepository,
    private val mChartTrackRepository: ChartTrackRepository,
    private val mEntityManager: EntityManager,
    private val mDayTrackRepository: DayTrackRepository,
    private val mArtistRepository: ArtistRepository,
    private val mGlobalRankTrackRepository: GlobalRankTrackRepository
) : ITrackController {

    @RequestMapping(value = ["/track/getById"], method = [RequestMethod.GET])
    fun track(@RequestParam(value = "id") id: Long): Track {
        val theOne = mTrackRepository.findById(id).orElse(null)
            ?: throw TrackNotFoundException()
        return theOne
    }

    @RequestMapping(value = ["/track/getByArtist"], method = [RequestMethod.GET])
    fun getTracksAPI(
        @RequestParam(name = "artist_id") artistId: Long,
        @RequestParam(required = false, defaultValue = "0") size: Int
    ): List<Track> {
        val theArtist = mArtistRepository.findById(artistId).orElse(null)
            ?: throw ArtistNotFoundException()
        return getTracks(theArtist, size)
    }

    override fun getTracks(artist: Artist, size: Int): List<Track> {
        val theTracks = mTrackRepository.findByArtist(artist)
        val theResult = mTrackRepository
            .sortByGlobalRank(TrackUtils.asTrackIds(theTracks), if (size == 0) theTracks.size else size)
        return theResult
    }

    @RequestMapping(value = ["track/best"], method = [RequestMethod.GET])
    fun bestTracks(
        @RequestParam(name = "chart_id") chartId: Long,
        @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING)
        from: String?,
        @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING)
        to: String?,
        @RequestParam(value = "size", required = false, defaultValue = "100") size: Int
    ): Iterable<Track> {
        @Suppress("UNCHECKED_CAST")
        val results = mEntityManager.createNativeQuery(TrackUtils.getBestTracksQuery(chartId, size, from, to))
            .resultList as List<Array<Any>>

        // Manually map Object[] results to Track entities
        return results.map { row ->
            val artist = Artist(
                id = (row[7] as? Number)?.toLong(),
                name = row[8] as String,
                nameNormalized = row[9] as? String
            )
            Track(
                id = (row[0] as? Number)?.toLong(),
                title = row[1] as String,
                artist = artist,
                artistName = row[3] as? String,
                firstChartDate = (row[4] as? java.sql.Date)?.toLocalDate(),
                peakGlobalRank = (row[5] as? Number)?.toInt(),
                totalWeeksOnChart = (row[6] as? Number)?.toInt() ?: 0
            )
        }
    }

    @RequestMapping(value = ["/track/day"], method = [RequestMethod.GET])
    fun dayTrack(
        @RequestParam(required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) date: String?
    ): DayTrack {
        val theOne = if (Ex.isNotEmpty(date))
            mDayTrackRepository.findById(java.sql.Date.valueOf(date)).orElse(null)
        else
            mDayTrackRepository.findLast(PageRequest.of(0, 1)).content.firstOrNull()

        return theOne ?: throw TrackNotFoundException()
    }

    @Transactional
    @RequestMapping(value = ["/track/day"], method = [RequestMethod.POST])
    fun dayTrack(
        @RequestParam() @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) date: String
    ) {
        updateDayTrack(date)
    }

    @RequestMapping(value = ["/track/history"], method = [RequestMethod.GET])
    fun getTrackHistoryAPI(
        @RequestParam() id: Long,
        @RequestParam(required = false, name = "chart_id") chartId: Long?
    ): Map<String, Map<String, Int>> {
        return getTrackHistory(id, chartId)
    }

    override fun getTrackHistory(id: Long, chartId: Long?): Map<String, Map<String, Int>> {
        val theTrack = mTrackRepository.findById(id).orElse(null)
            ?: throw TrackNotFoundException()
        val theRequestedChart = chartId?.let { if (Ex.isPositive(it)) mChartRepository.findById(it).orElse(null) else null }
        val theCharts: Iterable<Chart> =
            if (theRequestedChart == null) mChartRepository.findAll() else listOf(theRequestedChart)
        val theFullHistory = mutableMapOf<String, MutableMap<String, Int>>()
        val theChartTracks = mChartTrackRepository.findByTrackInCharts(theTrack, theCharts)
        for (theChartTrack in theChartTracks) {
            val theChartName = theChartTrack.chartList?.chart?.name ?: continue
            val weekDate = theChartTrack.chartList?.week?.date ?: continue
            val theChartHistory = theFullHistory.getOrPut(theChartName) { sortedMapOf() }
            theChartHistory[weekDate] = theChartTrack.rank
        }
        return theFullHistory
    }

    @RequestMapping(value = ["/track/info"], method = [RequestMethod.GET])
    fun getTrackInfo(@RequestParam() id: Long): TrackInfo {
        val theTrack = mTrackRepository.findById(id).orElse(null)
            ?: throw TrackNotFoundException()
        val theTrackInfo = TrackInfo()
        theTrackInfo.track = theTrack
        theTrackInfo.history = getTrackHistory(id, null)
        theTrackInfo.globalRank = theTrack.id?.let { mGlobalRankTrackRepository.findByTrackId(it)?.rank } ?: 0
        return theTrackInfo
    }

    @RequestMapping(value = ["/track/global"], method = [RequestMethod.GET])
    fun getGlobalTracks(
        @RequestParam() rank: Long,
        @RequestParam(required = false, defaultValue = "1") size: Long
    ): List<Track> {
        val theGlobalTracks = mTrackRepository.findGlobalList(rank, rank + size)
        return theGlobalTracks
    }

    override fun updateDayTrack(formattedDay: String): Track {
        val theTracksOfTheDay = getTracksOfTheDay(formattedDay, 10)
        val theTrack = theTracksOfTheDay[0]
        mDayTrackRepository.save(DayTrack(java.sql.Date.valueOf(formattedDay), theTrack, null))
        return theTrack
    }

    private fun getTracksOfTheDay(date: String, size: Int): List<Track> {
        val theTrackIds = mTrackRepository.findDebutsOfTheDay(date.substring(5))
        return mTrackRepository.sortByGlobalRank(theTrackIds, size)
    }
}
