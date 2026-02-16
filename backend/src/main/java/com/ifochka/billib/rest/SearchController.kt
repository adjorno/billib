package com.ifochka.billib.rest

import com.ifochka.billib.rest.db.Artist
import com.ifochka.billib.rest.db.ArtistUtils
import com.ifochka.billib.rest.db.Track
import com.ifochka.billib.rest.db.TrackUtils
import com.ifochka.billib.rest.model.MergedSearchResult
import com.ifochka.billib.rest.model.SearchResult
import com.m14n.ex.Ex
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger
import jakarta.persistence.EntityManager

@RestController
class SearchController(
    private val mEntityManager: EntityManager
) {

    companion object {
        private const val ALL_RESULTS = -1
        private const val NO_RESULTS = 0
        private const val MAX_RESULT_SIZE = 100

        private fun getKeywords(query: String?): Array<String> {
            val theSplits = query?.split("(\\)|\\])?(\\s+|^|$)(\\(|\\[)?".toRegex())?.toTypedArray() ?: emptyArray()
            val theKeywords = mutableListOf<String>()
            for (theSplit in theSplits) {
                if (isKeyword(theSplit)) {
                    theKeywords.add(theSplit)
                }
            }
            return theKeywords.toTypedArray()
        }

        private fun isKeyword(split: String): Boolean {
            return split.matches(".*\\w+.*".toRegex()) and
                    !split.matches(
                        ("(?i)" + "(and)" + "|(vs\\.?)" + "|(feat\\.?)" + "|(featuring)" + "|(presents)" + "|(^pres\\.?\$)" +
                                "|(introducing)" + "|(starr?ing)" + "|(y)").toRegex()
                    )
        }
    }

    @RequestMapping(value = ["/search"], method = [RequestMethod.GET])
    fun search(
        @RequestParam() query: String,
        @RequestParam(name = "artists_offset", required = false, defaultValue = "0") artistsOffset: Int,
        @RequestParam(name = "artists_size", required = false, defaultValue = "5") artistsSize: Int,
        @RequestParam(name = "tracks_offset", required = false, defaultValue = "0") tracksOffset: Int,
        @RequestParam(name = "tracks_size", required = false, defaultValue = "10") tracksSize: Int,
        @RequestParam(name = "alphabetical", required = false, defaultValue = "false") alphabetical: Boolean
    ): MergedSearchResult {
        val theResult = MergedSearchResult()
        val theKeywords = getKeywords(query)
        if (theKeywords.isNotEmpty()) {
            var theSearchQuery: String
            var adjustedArtistsSize = artistsSize
            if (adjustedArtistsSize != NO_RESULTS) {
                val theArtistSearchResult = SearchResult<Artist>()
                if (adjustedArtistsSize > MAX_RESULT_SIZE || adjustedArtistsSize == ALL_RESULTS) {
                    adjustedArtistsSize = MAX_RESULT_SIZE
                }
                theSearchQuery = ArtistUtils.getCountSearchQuery(theKeywords)
                val theTotal =
                    (mEntityManager.createNativeQuery(theSearchQuery).singleResult as Long).toInt()
                var theArtists = emptyList<Artist>()
                if (artistsOffset < theTotal) {
                    theSearchQuery = ArtistUtils.getSearchQuery(theKeywords, artistsOffset, adjustedArtistsSize, alphabetical)
                    @Suppress("UNCHECKED_CAST")
                    val resultList = mEntityManager.createNativeQuery(theSearchQuery, Artist::class.java).resultList as List<Artist>
                    theArtists = if (resultList.size > MAX_RESULT_SIZE) resultList.subList(0, MAX_RESULT_SIZE) else resultList
                }
                theArtistSearchResult.results = theArtists
                theArtistSearchResult.total = theTotal
                theArtistSearchResult.offset = artistsOffset
                theResult.artists = theArtistSearchResult
            }

            var adjustedTracksSize = tracksSize
            if (adjustedTracksSize != NO_RESULTS) {
                val theTrackSearchResult = SearchResult<Track>()
                theSearchQuery = TrackUtils.getCountSearchQuery(theKeywords)
                if (adjustedTracksSize > MAX_RESULT_SIZE || adjustedTracksSize == ALL_RESULTS) {
                    adjustedTracksSize = MAX_RESULT_SIZE
                }
                val theTotal = (mEntityManager.createNativeQuery(theSearchQuery).singleResult as Long)
                    .toInt()
                var theTracks = emptyList<Track>()
                if (artistsOffset < theTotal) {
                    theSearchQuery = TrackUtils.getSearchQuery(theKeywords, tracksOffset, adjustedTracksSize, alphabetical)
                    @Suppress("UNCHECKED_CAST")
                    val resultList = mEntityManager.createNativeQuery(theSearchQuery, Track::class.java).resultList as List<Track>
                    theTracks = if (resultList.size > MAX_RESULT_SIZE) resultList.subList(0, MAX_RESULT_SIZE) else resultList
                }
                theTrackSearchResult.results = theTracks
                theTrackSearchResult.total = theTotal
                theTrackSearchResult.offset = tracksOffset
                theResult.tracks = theTrackSearchResult
            }
        }
        return theResult
    }
}
