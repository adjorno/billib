package com.ifochka.m14n.rest

import com.ifochka.m14n.rest.catalog.domain.Artist
import com.ifochka.m14n.rest.catalog.domain.ArtistRelationRepository
import com.ifochka.m14n.rest.catalog.domain.ArtistRepository
import com.ifochka.m14n.rest.catalog.domain.ArtistUtils
import com.ifochka.m14n.rest.catalog.domain.Track
import com.ifochka.m14n.rest.catalog.domain.TrackRepository
import com.ifochka.m14n.rest.chart.domain.ChartList
import com.ifochka.m14n.rest.chart.domain.ChartListRepository
import com.ifochka.m14n.rest.chart.domain.ChartTrack
import com.ifochka.m14n.rest.chart.domain.ChartTrackRepository
import com.ifochka.m14n.rest.chart.rest.ChartTrackController
import com.ifochka.m14n.rest.db.DuplicateArtist
import com.ifochka.m14n.rest.db.DuplicateArtistRepository
import com.ifochka.m14n.rest.db.DuplicateTrack
import com.ifochka.m14n.rest.db.DuplicateTrackRepository
import com.ifochka.m14n.rest.model.MergeOperation
import com.ifochka.m14n.rest.shared.ArtistNotFoundException
import com.ifochka.m14n.rest.shared.TrackNotFoundException
import com.ifochka.m14n.rest.trends.domain.DayTrackRepository
import com.ifochka.m14n.rest.trends.domain.TrendTrackRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DuplicateController(
    private val mChartListRepository: ChartListRepository,
    private val mArtistRepository: ArtistRepository,
    private val mTrackRepository: TrackRepository,
    private val mDuplicateArtistRepository: DuplicateArtistRepository,
    private val mDuplicateTrackRepository: DuplicateTrackRepository,
    private val mChartTrackRepository: ChartTrackRepository,
    private val mDayTrackRepository: DayTrackRepository,
    private val mTrendTrackRepository: TrendTrackRepository,
    private val mChartTrackController: ChartTrackController,
    private val mArtistRelationRepository: ArtistRelationRepository,
) : IDuplicateController {
    @Transactional
    @RequestMapping(value = ["/duplicate/checkTracks"], method = [RequestMethod.POST])
    fun checkTracksAPI(
        @RequestParam(name = "password") password: String,
        @RequestParam(name = "fromArtist", defaultValue = "1") from: Int,
        @RequestParam(name = "checkSize", defaultValue = "100") size: Int,
    ) {
        val theArtists = mArtistRepository.findAll().toList()
        val theLeftDuplicates = mutableMapOf<Track, Track>()
        for (a in from until theArtists.size) {
            val theTracks = mTrackRepository.findByArtist(theArtists[a])
            for (t1 in theTracks.indices) {
                val theTrack1 = theTracks[t1]
                for (t2 in (t1 + 1) until theTracks.size) {
                    val theTrack2 = theTracks[t2]
                    if (theTrack1.id != theTrack2.id) {
                        if (equalsValued(theTrack1.title, theTrack2.title)) {
                            val l1 = getValuedLength(theTrack1.title)
                            val l2 = getValuedLength(theTrack2.title)
                            when {
                                l1 > l2 -> {
                                    removeDuplicateTrack(theTrack1.id!!, theTrack2.id!!)
                                    println("${theTrack2.title} => ${theTrack1.title}")
                                }
                                l2 > l1 -> {
                                    removeDuplicateTrack(theTrack2.id!!, theTrack1.id!!)
                                    println("${theTrack1.title} => ${theTrack2.title}")
                                }
                                else -> {
                                    theLeftDuplicates[theTrack1] = theTrack2
                                }
                            }
                        }
                    }
                }
            }
            if (a - from > size) {
                break
            }
        }
        println("FINISHED")
        println("POSSIBLE TRACK DUPLICATES:")
        for ((fromTrack, toTrack) in theLeftDuplicates) {
            println("${fromTrack.id} ${toTrack.id} (${fromTrack.title} => ${toTrack.title})")
        }
    }

    @Transactional
    @RequestMapping(value = ["/duplicate/checkArtists"], method = [RequestMethod.POST])
    fun checkArtistsAPI(
        @RequestParam(name = "password") password: String,
        @RequestParam(name = "fromArtist", defaultValue = "1") from: Int,
        @RequestParam(name = "checkSize", defaultValue = "100") size: Int,
    ) {
        val theArtists = mArtistRepository.findAll().toList()
        val theLeftDuplicates = mutableMapOf<Artist, Artist>()
        for (i in from until theArtists.size) {
            val theArtist1 = theArtists[i]
            for (j in (i + 1) until theArtists.size) {
                val theArtist2 = theArtists[j]
                if (equalsValued(theArtist1.name, theArtist2.name)) {
                    val l1 = getValuedLength(theArtist1.name)
                    val l2 = getValuedLength(theArtist2.name)
                    when {
                        l1 > l2 -> {
                            removeDuplicateArtist(theArtist1.id!!, theArtist2.id!!)
                            println("${theArtist2.name} => ${theArtist1.name}")
                        }
                        l2 > l1 -> {
                            removeDuplicateArtist(theArtist2.id!!, theArtist1.id!!)
                            println("${theArtist1.name} => ${theArtist2.name}")
                        }
                        else -> {
                            theLeftDuplicates[theArtist1] = theArtist2
                        }
                    }
                }
            }
            if (i - from > size) {
                break
            }
        }
        println("FINISHED")
        println("POSSIBLE ARTIST DUPLICATES:")
        for ((fromArtist, toArtist) in theLeftDuplicates) {
            println("${fromArtist.id} ${toArtist.id} (${fromArtist.name} => ${toArtist.name})")
        }
    }

    @Transactional
    @RequestMapping(value = ["/duplicate/checkLastWeek"], method = [RequestMethod.POST])
    fun checkLastWeekAPI(
        @RequestParam(name = "password") password: String,
        @RequestParam(name = "from", defaultValue = "1") from: Long,
        @RequestParam(name = "size", defaultValue = "500") size: Long,
    ) {
        checkLastWeek(from, size)
    }

    @Transactional
    @RequestMapping(value = ["/duplicate/artist"], method = [RequestMethod.GET])
    fun removeDuplicateArtistAPI(
        @RequestParam(name = "password") password: String,
        @RequestParam(name = "originalArtistId") originalId: Long,
        @RequestParam(name = "duplicateArtistId") duplicateId: Long,
    ): List<MergeOperation<*>>? {
        val theMergeOperations = removeDuplicateArtist(originalId, duplicateId)
        if (!theMergeOperations.isNullOrEmpty()) {
            return theMergeOperations
        } else {
            throw ArtistNotFoundException()
        }
    }

    @Transactional
    @RequestMapping(value = ["/duplicate/track"], method = [RequestMethod.GET])
    fun removeDuplicateTrackAPI(
        @RequestParam(name = "password") password: String,
        @RequestParam(name = "originalTrackId") originalId: Long,
        @RequestParam(name = "duplicateTrackId") duplicateId: Long,
    ): MergeOperation<Track>? {
        val theTrackMergeOperation = removeDuplicateTrack(originalId, duplicateId)
        return theTrackMergeOperation ?: throw TrackNotFoundException()
    }

    @Transactional
    @RequestMapping(value = ["/duplicate/checkCollaborations"], method = [RequestMethod.POST])
    fun removeDuplicateCollaborationsAPI(
        @RequestParam(name = "password") password: String,
        @RequestParam(defaultValue = "0") from: Int,
        @RequestParam(required = false, defaultValue = "100") size: Int,
        @RequestParam() dryRun: Boolean,
    ) {
        val theArtists = mArtistRepository.findAll().toList()
        println("STARTED")
        val fs = mutableSetOf<String>()
        for (i in from until theArtists.size) {
            val theArtist1 = theArtists[i]
            val a = ArtistUtils.splitCollaboration(theArtist1.name!!)
            val a1l = a.size
            if (a1l == 1) {
                continue
            }
            val a1 = mutableSetOf<String>()
            a1.addAll(a)
            for (j in (i + 1) until theArtists.size) {
                val theArtist2 = theArtists[j]
                val b = ArtistUtils.splitCollaboration(theArtist2.name!!)
                if (b.size != a1l) {
                    continue
                }
                val b1 = mutableSetOf<String>()
                b1.addAll(b)
                if (a1 == b1) {
                    var toRemove = theArtist2
                    var toMerge = theArtist1
                    if (theArtist1.name!!.contains("and") || theArtist1.name!!.contains("And")) {
                        toRemove = theArtist1
                        toMerge = theArtist2
                    }

                    println("${toRemove.id} => ${toMerge.id}, ${toRemove.name} => ${toMerge.name}")
                    if (!dryRun) {
                        removeDuplicateArtist(toMerge.id!!, toRemove.id!!)
                    }
                }
            }
            if (i % 100 == 0) {
                println("CHECKED $i")
            }
            if (i - from >= size) {
                break
            }
        }
        println("FINISHED")
        for (f in fs) {
            println(f)
        }
    }

    override fun checkLastWeek(
        from: Long,
        size: Long,
    ) {
        val theSize = mChartListRepository.count()
        var thePreviousChartList: ChartList? = null
        var thePreviousChartTracks = mutableListOf<ChartTrack>()
        val thePossibleArtistUpdates = mutableMapOf<Artist, Artist>()
        val start = from
        var currentFrom = from
        println("CHECK STARTED")
        while (currentFrom <= theSize) {
            val theChartList = mChartListRepository.findById(currentFrom).orElse(null)
            val theChartTracks = mChartTrackRepository.findByChartList(theChartList)

            if (thePreviousChartList != null &&
                thePreviousChartList.chart?.id != theChartList.chart?.id
            ) {
                thePreviousChartList = null
                thePreviousChartTracks = mutableListOf()
            }

            if (thePreviousChartList == null) {
                thePreviousChartList = mChartListRepository.findById(theChartList.previousChartListId!!).orElse(null)
            }
            if (thePreviousChartTracks.isEmpty() && thePreviousChartList != null) {
                thePreviousChartTracks = mChartTrackRepository.findByChartList(thePreviousChartList).toMutableList()
            }
            if (thePreviousChartTracks.isNotEmpty()) {
                for (theChartTrack in theChartTracks) {
                    val theLastWeekRank = theChartTrack.lastWeekRank
                    if (theLastWeekRank != 0 && theLastWeekRank <= theChartList.chart!!.listSize!!) {
                        var found = false
                        val theSameRankTracks = mutableListOf<ChartTrack>()
                        for (thePreviousChartTrack in thePreviousChartTracks) {
                            if (thePreviousChartTrack.rank == theLastWeekRank) {
                                theSameRankTracks.add(thePreviousChartTrack)
                                if (theChartTrack.track?.id == thePreviousChartTrack.track?.id) {
                                    found = true
                                    break
                                }
                            }
                        }
                        if (!found) {
                            if (theSameRankTracks.isEmpty()) {
                                // try to find the same track in previous list
                                val theSameTrack = mChartTrackRepository
                                    .findByTrackAndChartList(theChartTrack.track!!, thePreviousChartList!!)
                                if (theSameTrack != null) {
                                    mChartTrackRepository.updateRank(theSameTrack, theLastWeekRank)
                                    mChartTrackRepository.updateLastWeekRank(theChartTrack, theSameTrack.rank)
                                    println("FIXED! UPDATED LAST WEEK RANK")
                                    continue
                                } else {
                                    if (mChartTrackRepository.countRealChartListSize(thePreviousChartList).size <
                                        theChartList.chart!!.listSize!!
                                    ) {
                                        val theMissingTrack = mChartTrackController
                                            .addMissingTrackInternal(
                                                thePreviousChartList,
                                                theChartTrack.track!!,
                                                theLastWeekRank,
                                            )
                                        println("FIXED! ADDED MISSING TRACK - $theMissingTrack")
                                        continue
                                    }
                                }
                            } else {
                                if (theSameRankTracks.size == 1) {
                                    val theDuplicate = theSameRankTracks[0]
                                    if (theDuplicate.track?.artist?.id == theChartTrack.track?.artist?.id) {
                                        val l1 = getValuedLength(theDuplicate.track!!.title)
                                        val l2 = getValuedLength(theChartTrack.track!!.title)
                                        val original = if (l1 >= l2) theDuplicate.track else theChartTrack.track
                                        val duplicate = if (l1 >= l2) theChartTrack.track else theDuplicate.track
                                        val theTrackMergeOperation =
                                            removeDuplicateTrack(original!!.id!!, duplicate!!.id!!)
                                        if (theTrackMergeOperation != null) {
                                            theChartTrack.track = theTrackMergeOperation.mergedTo
                                        } else {
                                            print("NOT ")
                                        }
                                        println(
                                            "FIXED! REMOVE DUPLICATE TRACK - $duplicate ${duplicate.id} => $original ${original.id}",
                                        )
                                        continue
                                    } else {
                                        if (equalsValued(
                                                theDuplicate.track?.title,
                                                theChartTrack.track?.title,
                                            )
                                        ) {
                                            val l1 = getValuedLength(theDuplicate.track!!.artist?.name)
                                            val l2 = getValuedLength(theChartTrack.track!!.artist?.name)
                                            val original =
                                                if (l1 >= l2) theDuplicate.track else theChartTrack.track
                                            val duplicate =
                                                if (l1 >= l2) theChartTrack.track else theDuplicate.track
                                            val theTrackMergeOperation =
                                                removeDuplicateTrack(original!!.id!!, duplicate!!.id!!)
                                            if (theTrackMergeOperation != null) {
                                                theChartTrack.track = theTrackMergeOperation.mergedTo
                                            } else {
                                                print("NOT ")
                                            }
                                            println(
                                                "FIXED! REMOVE DUPLICATE TRACK - $duplicate ${duplicate.id} => $original ${original.id}",
                                            )
                                            thePossibleArtistUpdates[original.artist!!] = duplicate.artist!!
                                            continue
                                        }
                                    }
                                }
                            }
                            reportLastWeekRankProblem(
                                thePreviousChartList!!,
                                theChartList,
                                theChartTrack,
                                theSameRankTracks,
                            )
                        }
                    }
                }
            }
            if (++currentFrom % 500 == 0L) {
                println("CHECK $currentFrom")
            }
            if (currentFrom - start > size) {
                break
            }
            thePreviousChartList = theChartList
            thePreviousChartTracks = theChartTracks.toMutableList()
        }
        println("CHECK FINISHED")
        println("POSSIBLE ARTIST DUPLICATES:")
        for ((fromArtist, toArtist) in thePossibleArtistUpdates) {
            println("${fromArtist.id} ${toArtist.id} (${fromArtist.name} => ${toArtist.name})")
        }
    }

    private fun removeDuplicateArtist(
        originalId: Long,
        duplicateId: Long,
    ): List<MergeOperation<*>>? {
        val theOriginalArtist = mArtistRepository.findById(originalId).orElse(null)
        val theDuplicateArtist = mArtistRepository.findById(duplicateId).orElse(null)
        if (theOriginalArtist != null && theDuplicateArtist != null) {
            val theMerges = mutableListOf<MergeOperation<*>>()
            val theRepeatTitles =
                mTrackRepository.findRepeatTitles(theDuplicateArtist.id!!, theOriginalArtist.id!!)
            for (theTitle in theRepeatTitles) {
                val originalTrackId = mTrackRepository.findByTitleAndArtist(theTitle, theOriginalArtist)?.id
                val duplicateTrackId = mTrackRepository.findByTitleAndArtist(theTitle, theDuplicateArtist)?.id

                if (originalTrackId == null || duplicateTrackId == null) {
                    println("Could not find track for title - $theTitle")
                    continue
                }

                val theTrackMergeOperation = removeDuplicateTrack(originalTrackId, duplicateTrackId)
                if (theTrackMergeOperation == null) {
                    println("Could not merge the track - $theTitle")
                } else {
                    theMerges.add(theTrackMergeOperation)
                }
            }
            mTrackRepository.updateArtists(theDuplicateArtist, theOriginalArtist)

            // Find and delete relations that would become duplicates after merging
            val mergingCollaborationIds = mArtistRelationRepository
                .findMergingCollaborationIds(theDuplicateArtist.id!!, theOriginalArtist.id!!)
            for (mergeCollaborationId in mergingCollaborationIds) {
                mArtistRelationRepository
                    .deleteByCollaborationIdAndMemberId(mergeCollaborationId, theDuplicateArtist.id!!)
            }
            mArtistRelationRepository.updateMemberArtist(theDuplicateArtist, theOriginalArtist)

            val mergingMemberIds =
                mArtistRelationRepository.findMergingMemberIds(theDuplicateArtist.id!!, theOriginalArtist.id!!)
            for (mergingMemberId in mergingMemberIds) {
                mArtistRelationRepository
                    .deleteByCollaborationIdAndMemberId(theDuplicateArtist.id!!, mergingMemberId)
            }
            mArtistRelationRepository.updateCollaborationArtist(theDuplicateArtist, theOriginalArtist)
            mDuplicateArtistRepository.updateArtists(theDuplicateArtist, theOriginalArtist)
            mDuplicateArtistRepository.save(DuplicateArtist(theDuplicateArtist.name!!, theOriginalArtist))

            mArtistRepository.delete(theDuplicateArtist)
            theMerges.add(MergeOperation(theDuplicateArtist, theOriginalArtist))
            return theMerges
        }
        return null
    }

    private fun removeDuplicateTrack(
        originalId: Long,
        duplicateId: Long,
    ): MergeOperation<Track>? {
        val duplicateTrack = mTrackRepository.findById(duplicateId).orElse(null)
        val originalTrack = mTrackRepository.findById(originalId).orElse(null)
        if (originalTrack != null && duplicateTrack != null) {
            mChartTrackRepository.updateTracks(duplicateTrack, originalTrack)
            mDayTrackRepository.updateTracks(duplicateTrack, originalTrack)
            mTrendTrackRepository.updateTracks(duplicateTrack, originalTrack)
            mDuplicateTrackRepository.updateTracks(duplicateTrack, originalTrack)
            mDuplicateTrackRepository.save(
                DuplicateTrack(
                    originalTrack.artist!!.generateDuplicateTitle(duplicateTrack.title!!),
                    originalTrack,
                ),
            )

            mTrackRepository.delete(duplicateTrack)
            return MergeOperation(duplicateTrack, originalTrack)
        }
        return null
    }

    private fun reportLastWeekRankProblem(
        previousChartList: ChartList,
        chartList: ChartList,
        theChartTrack: ChartTrack,
        sameRankTracks: List<ChartTrack>,
    ) {
        println("****************")
        println("--- ORIGINAL TRACK FROM CHART LIST ${chartList.id}")
        println("$theChartTrack TRACK_ID = ${theChartTrack.track?.id}")
        println(
            "--- POSSIBLE TRACKS FROM PREVIOUS CHART_LIST ${previousChartList.id} AND _RANK ${theChartTrack.lastWeekRank}",
        )
        for (thePrevious in sameRankTracks) {
            println("$thePrevious ${thePrevious.track?.id}")
        }
    }

    private fun equalsValued(
        s1: String?,
        s2: String?,
    ): Boolean {
        val v1 = (s1 ?: "").replace(Regex("[^\\w]"), "")
        val v2 = (s2 ?: "").replace(Regex("[^\\w]"), "")
        return v1.equals(v2, ignoreCase = true)
    }

    private fun getValuedLength(s: String?): Int = (s ?: "").replace(Regex("\\s"), "").length
}
