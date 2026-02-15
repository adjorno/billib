package com.adjorno.billib.rest.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface TrackRepository : CrudRepository<Track, Long> {

    @Query(value = "SELECT CT1.TRACK_ID FROM CHART_TRACK CT1\n" +
            "INNER JOIN CHART_LIST ON CHART_LIST._ID = CT1.CHART_LIST_ID\n" +
            "INNER JOIN WEEK ON WEEK.WEEK_ID = CHART_LIST.WEEK_ID\n" +
            "INNER JOIN TRACK ON TRACK._ID = CT1.TRACK_ID\n" + "INNER JOIN\n" + "  (\n" +
            "    SELECT TRACK_ID, MIN(WEEK.DATE) MINWEEK FROM CHART_TRACK CT2\n" +
            "    INNER JOIN CHART_LIST ON CHART_LIST._ID = CT2.CHART_LIST_ID\n" +
            "    INNER JOIN WEEK ON WEEK.WEEK_ID = CHART_LIST.WEEK_ID\n" + "    GROUP BY TRACK_ID\n" +
            "  ) T ON CT1.TRACK_ID = T.TRACK_ID AND DATE = T.MINWEEK\n" + "WHERE DATE LIKE CONCAT(\"%-\", ?1)",
            nativeQuery = true)
    fun findDebutsOfTheDay(day: String): List<Long>

    @Query(value = "SELECT TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, TRACK.ARTIST_NAME, TRACK.FIRST_CHART_DATE, TRACK.PEAK_GLOBAL_RANK, TRACK.TOTAL_WEEKS_ON_CHART FROM GLOBAL_RANK_TRACK\n" +
            "INNER JOIN TRACK ON TRACK._ID = GLOBAL_RANK_TRACK.TRACK_ID\n" +
            "INNER JOIN ARTIST ON ARTIST._id = TRACK.ARTIST_ID\n" + "WHERE TRACK_ID IN (?1)\n" +
            "ORDER BY GLOBAL_RANK_TRACK.RANK\n" + "LIMIT ?2", nativeQuery = true)
    fun sortByGlobalRank(ids: List<Long>, size: Int): List<Track>

    fun findByTitleAndArtist(trackTitle: String, artist: Artist): Track?

    @Query(value = "SELECT TRACK.TITLE FROM TRACK\n" + "WHERE ARTIST_ID IN (?1, ?2)\n" + "GROUP BY TRACK.TITLE\n" +
            "HAVING COUNT(*) > 1\n", nativeQuery = true)
    fun findRepeatTitles(artistId1: Long, artistId2: Long): List<String>

    @Modifying
    @Query(value = "update Track t set t.artist = ?2 where t.artist = ?1")
    fun updateArtists(from: Artist, to: Artist)

    fun findByArtist(artist: Artist): List<Track>

    @Query(value = "SELECT TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, TRACK.ARTIST_NAME, TRACK.FIRST_CHART_DATE, TRACK.PEAK_GLOBAL_RANK, TRACK.TOTAL_WEEKS_ON_CHART FROM GLOBAL_RANK_TRACK\n" +
            "INNER JOIN TRACK ON TRACK._ID = GLOBAL_RANK_TRACK.TRACK_ID\n" +
            "INNER JOIN ARTIST ON ARTIST._id = TRACK.ARTIST_ID\n" +
            "WHERE GLOBAL_RANK_TRACK.RANK >= ?1 AND GLOBAL_RANK_TRACK.RANK < ?2\n" +
            "ORDER BY GLOBAL_RANK_TRACK.RANK", nativeQuery = true)
    fun findGlobalList(from: Long, to: Long): List<Track>
}
