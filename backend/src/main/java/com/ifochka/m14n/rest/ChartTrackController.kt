package com.ifochka.m14n.rest

import com.ifochka.m14n.rest.db.ChartList
import com.ifochka.m14n.rest.db.ChartListRepository
import com.ifochka.m14n.rest.db.ChartTrack
import com.ifochka.m14n.rest.db.ChartTrackRepository
import com.ifochka.m14n.rest.db.Track
import com.ifochka.m14n.rest.db.TrackRepository
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChartTrackController(
    private val mChartListRepository: ChartListRepository,
    private val mChartTrackRepository: ChartTrackRepository,
    private val mTrackRepository: TrackRepository,
) {
    @Transactional
    @RequestMapping(value = ["/chartTrack/updateTrack"], method = [RequestMethod.POST])
    fun updateTrack(
        @RequestParam(name = "password") password: String,
        @RequestParam(name = "chartTrackId") chartTrackId: Long,
        @RequestParam(name = "trackId") trackId: Long,
    ) {
        val theNewTrack = mTrackRepository.findById(trackId).orElse(null)
        val theChartTrack = mChartTrackRepository.findById(chartTrackId).orElse(null)
        if (theChartTrack == null && theNewTrack == null) {
            throw TrackNotFoundException()
        }
        mChartTrackRepository.updateTrack(theChartTrack, theNewTrack)
    }

    @Transactional
    @RequestMapping(value = ["/chartTrack/updateRank"], method = [RequestMethod.POST])
    fun updateRank(
        @RequestParam(name = "chartTrackId") chartTrackId: Long,
        @RequestParam(name = "rank") rank: Int,
    ) {
        val theChartTrack = mChartTrackRepository.findById(chartTrackId).orElse(null)
            ?: throw TrackNotFoundException()
        mChartTrackRepository.updateRank(theChartTrack, rank)
    }

    @Transactional
    @RequestMapping(value = ["/chartTrack/updateLastWeekRanks"], method = [RequestMethod.POST])
    fun updateLastWeekRanks(
        @RequestParam(name = "chartListId") chartListId: Long,
    ) {
        val theChartListTracks =
            mChartTrackRepository.findByChartList(mChartListRepository.findById(chartListId).orElse(null))
        for (ct in theChartListTracks) {
            val chartTrackId = ct.id ?: continue
            val thePreviousWeekRanks = mChartTrackRepository.findPreviousWeekRank(chartTrackId)
            val theLast = if (thePreviousWeekRanks.isEmpty()) 0 else thePreviousWeekRanks[0]
            mChartTrackRepository.updateLastWeekRank(ct, theLast)
        }
    }

    @Transactional
    @RequestMapping(value = ["/chartTrack/updateMissingTrack"], method = [RequestMethod.POST])
    fun addMissingChartTrack(
        @RequestParam(name = "password") password: String,
        @RequestParam(name = "clId") chartListId: Long,
        @RequestParam(name = "tId") trackId: Long,
        @RequestParam(name = "rank") rank: Int,
    ) {
        val theTrack = mTrackRepository.findById(trackId).orElse(null)
            ?: throw TrackNotFoundException()
        val theChartList = mChartListRepository.findById(chartListId).orElse(null)
            ?: throw ChartListNotFoundException()
    }

    fun getDebut(track: Track): ChartTrack = mChartTrackRepository.findByTrackAndSort(track, Sort.by("w.date")).first()

    fun addMissingTrackInternal(
        chartList: ChartList,
        track: Track,
        rank: Int,
    ): ChartTrack {
        var theLastWeekRank = 0
        val thePreviousChartList = chartList.previousChartListId?.let {
            mChartListRepository.findById(it).orElse(null)
        }
        if (thePreviousChartList != null) {
            val thePrevious = mChartTrackRepository.findByTrackAndChartList(track, thePreviousChartList)
            if (thePrevious != null) {
                theLastWeekRank = thePrevious.rank
            }
        }
        val theMissingChartTrack = ChartTrack()
        theMissingChartTrack.lastWeekRank = theLastWeekRank
        theMissingChartTrack.chartList = chartList
        theMissingChartTrack.track = track
        theMissingChartTrack.rank = rank
        mChartTrackRepository.save(theMissingChartTrack)
        return theMissingChartTrack
    }
}
