package com.ifochka.m14n.rest

import com.ifochka.m14n.rest.db.Artist
import com.ifochka.m14n.rest.db.ArtistRepository
import com.ifochka.m14n.rest.db.DayTrack
import com.ifochka.m14n.rest.db.DayTrackRepository
import com.ifochka.m14n.rest.db.GlobalRankTrackRepository
import com.ifochka.m14n.rest.db.Track
import com.ifochka.m14n.rest.db.TrackRepository
import com.ifochka.m14n.rest.db.TrackUtils
import com.ifochka.m14n.rest.model.TrackInfo
import com.ifochka.m14n.rest.notification.FcmService
import com.ifochka.m14n.rest.shared.ArtistNotFoundException
import com.ifochka.m14n.rest.shared.M14n
import com.ifochka.m14n.rest.shared.TrackNotFoundException
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TrackController(
    private val mTrackRepository: TrackRepository,
    private val mEntityManager: EntityManager,
    private val mDayTrackRepository: DayTrackRepository,
    private val mArtistRepository: ArtistRepository,
    private val mGlobalRankTrackRepository: GlobalRankTrackRepository,
    private val mFcmService: FcmService,
    private val mTrackService: TrackService,
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

        // Manually map Object[] results to Track entities
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

    @RequestMapping(value = ["/track/day"], method = [RequestMethod.GET])
    fun dayTrack(
        @RequestParam(required = false) @DateTimeFormat(pattern = M14n.CHART_DATE_FORMAT_STRING) date: String?,
    ): DayTrack {
        val theOne = if (!date.isNullOrEmpty()) {
            mDayTrackRepository.findByDay(java.sql.Date.valueOf(date))
        } else {
            mDayTrackRepository.findLast(PageRequest.of(0, 1)).content.firstOrNull()
        }

        return theOne ?: throw TrackNotFoundException()
    }

    @Transactional
    @RequestMapping(value = ["/track/day"], method = [RequestMethod.POST])
    fun dayTrack(
        @RequestParam() @DateTimeFormat(pattern = M14n.CHART_DATE_FORMAT_STRING) date: String,
    ) {
        updateDayTrack(date)
    }

    @RequestMapping(value = ["/track/history"], method = [RequestMethod.GET])
    fun getTrackHistoryAPI(
        @RequestParam() id: Long,
        @RequestParam(required = false, name = "chart_id") chartId: Long?,
    ): Map<String, Map<String, Int>> = getTrackHistory(id, chartId)

    override fun getTrackHistory(
        id: Long,
        chartId: Long?,
    ): Map<String, Map<String, Int>> = mTrackService.getTrackHistory(id, chartId)

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

    // TODO(#75): Gate with access token before re-enabling
    // @RequestMapping(value = ["/track/random"], method = [RequestMethod.POST])
    fun sendRandomTrackNotification(): Track {
        val track = mTrackRepository.findRandom() ?: throw TrackNotFoundException()
        val body = "${track.artist?.name ?: track.artistName} — ${track.title}"
        mFcmService.sendToTopic(
            topic = "track-of-day",
            title = "Track of the Day",
            body = body,
        )
        return track
    }

    @RequestMapping(value = ["/track/global"], method = [RequestMethod.GET])
    fun getGlobalTracks(
        @RequestParam() rank: Long,
        @RequestParam(required = false, defaultValue = "1") size: Long,
    ): List<Track> {
        val theGlobalTracks = mTrackRepository.findGlobalList(rank, rank + size)
        return theGlobalTracks
    }

    override fun updateDayTrack(formattedDay: String): Track {
        val theTracksOfTheDay = getTracksOfTheDay(formattedDay, 10)
        val theTrack = theTracksOfTheDay[0]
        val existing = mDayTrackRepository.findByDay(java.sql.Date.valueOf(formattedDay))
        mDayTrackRepository.save(
            DayTrack(
                id = existing?.id,
                day = java.sql.Date.valueOf(formattedDay),
                track = theTrack,
            ),
        )
        return theTrack
    }

    private fun getTracksOfTheDay(
        date: String,
        size: Int,
    ): List<Track> {
        val theTrackIds = mTrackRepository.findDebutsOfTheDay(date.substring(5))
        return mTrackRepository.sortByGlobalRank(theTrackIds, size)
    }
}
