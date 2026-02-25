package com.ifochka.m14n.rest.catalog.rest

import com.ifochka.m14n.rest.catalog.domain.Artist
import com.ifochka.m14n.rest.catalog.domain.ArtistRepository
import com.ifochka.m14n.rest.catalog.domain.Track
import com.ifochka.m14n.rest.catalog.domain.TrackHistoryPort
import com.ifochka.m14n.rest.catalog.domain.TrackRepository
import com.ifochka.m14n.rest.catalog.domain.TrackUtils
import com.ifochka.m14n.rest.catalog.rest.dtos.TrackInfo
import com.ifochka.m14n.rest.rankings.domain.GlobalRankTrackRepository
import com.ifochka.m14n.rest.shared.ArtistNotFoundException
import com.ifochka.m14n.rest.shared.M14n
import com.ifochka.m14n.rest.shared.TrackNotFoundException
import jakarta.persistence.EntityManager
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TrackController(
    private val mTrackRepository: TrackRepository,
    private val mEntityManager: EntityManager,
    private val mArtistRepository: ArtistRepository,
    private val mGlobalRankTrackRepository: GlobalRankTrackRepository,
    private val mTrackHistoryPort: TrackHistoryPort,
) : ITrackController {
    @RequestMapping(value = ["/track/getById"], method = [RequestMethod.GET])
    fun track(
        @RequestParam(value = "id") id: Long,
    ): Track {
        val theOne = mTrackRepository.findById(id).orElse(null)
            ?: throw TrackNotFoundException()
        return theOne
    }

    @RequestMapping(value = ["/track/getByArtist"], method = [RequestMethod.GET])
    fun getTracksAPI(
        @RequestParam(name = "artist_id") artistId: Long,
        @RequestParam(required = false, defaultValue = "0") size: Int,
    ): List<Track> {
        val theArtist = mArtistRepository.findById(artistId).orElse(null)
            ?: throw ArtistNotFoundException()
        return getTracks(theArtist, size)
    }

    override fun getTracks(
        artist: Artist,
        size: Int,
    ): List<Track> {
        val theTracks = mTrackRepository.findByArtist(artist)
        val theResult = mTrackRepository
            .sortByGlobalRank(TrackUtils.asTrackIds(theTracks), if (size == 0) theTracks.size else size)
        return theResult
    }

    @RequestMapping(value = ["track/best"], method = [RequestMethod.GET])
    fun bestTracks(
        @RequestParam(name = "chart_id") chartId: Long,
        @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = M14n.CHART_DATE_FORMAT_STRING)
        from: String?,
        @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = M14n.CHART_DATE_FORMAT_STRING)
        to: String?,
        @RequestParam(value = "size", required = false, defaultValue = "100") size: Int,
    ): Iterable<Track> {
        @Suppress("UNCHECKED_CAST")
        val results = mEntityManager.createNativeQuery(TrackUtils.getBestTracksQuery(chartId, size, from, to))
            .resultList as List<Array<Any>>

        return results.map { row ->
            val artist = Artist(
                id = (row[7] as? Number)?.toLong(),
                name = row[8] as String,
                nameNormalized = row[9] as? String,
            )
            Track(
                id = (row[0] as? Number)?.toLong(),
                title = row[1] as String,
                artist = artist,
                artistName = row[3] as? String,
                firstChartDate = (row[4] as? java.sql.Date)?.toLocalDate(),
                peakGlobalRank = (row[5] as? Number)?.toInt(),
                totalWeeksOnChart = (row[6] as? Number)?.toInt() ?: 0,
            )
        }
    }

    @RequestMapping(value = ["/track/history"], method = [RequestMethod.GET])
    fun getTrackHistoryAPI(
        @RequestParam() id: Long,
        @RequestParam(required = false, name = "chart_id") chartId: Long?,
    ): Map<String, Map<String, Int>> = getTrackHistory(id, chartId)

    override fun getTrackHistory(
        id: Long,
        chartId: Long?,
    ): Map<String, Map<String, Int>> = mTrackHistoryPort.getTrackHistory(id, chartId)

    @RequestMapping(value = ["/track/info"], method = [RequestMethod.GET])
    fun getTrackInfo(
        @RequestParam() id: Long,
    ): TrackInfo {
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
        @RequestParam(required = false, defaultValue = "1") size: Long,
    ): List<Track> {
        val theGlobalTracks = mTrackRepository.findGlobalList(rank, rank + size)
        return theGlobalTracks
    }
}
