package com.ifochka.m14n.rest.catalog.domain

import com.ifochka.m14n.rest.chart.domain.ChartTrack
import com.ifochka.m14n.rest.trends.domain.TrendTrack

object TrackUtils {
    @JvmStatic
    fun asTracks(tracks: List<ChartTrack>): List<Track> = tracks.mapNotNull { it.track }

    @JvmStatic
    fun asTrackList(trendTracks: List<TrendTrack>): List<Track> = trendTracks.mapNotNull { it.track }

    @JvmStatic
    fun asTrackIds(tracks: List<Track>): List<Long> = tracks.mapNotNull { it.id }

    @JvmStatic
    fun getBestTracksQuery(
        chartId: Long,
        limit: Int,
        from: String?,
        to: String?,
    ): String {
        val theQueryBuilder = StringBuilder(
            "SELECT TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, TRACK.ARTIST_NAME, " +
                "TRACK.FIRST_CHART_DATE, TRACK.PEAK_GLOBAL_RANK, TRACK.TOTAL_WEEKS_ON_CHART, " +
                "ARTIST._ID, ARTIST.NAME, ARTIST.NAME_NORMALIZED " +
                "FROM CHART_LIST " +
                "INNER JOIN CHART_TRACK ON CHART_LIST._ID = CHART_TRACK.CHART_LIST_ID " +
                "INNER JOIN CHART ON CHART._ID = CHART_LIST.CHART_ID " +
                "INNER JOIN TRACK ON TRACK._ID = CHART_TRACK.TRACK_ID " +
                "INNER JOIN WEEK ON WEEK.WEEK_ID = CHART_LIST.WEEK_ID " +
                "INNER JOIN ARTIST ON ARTIST._ID = TRACK.ARTIST_ID " +
                "WHERE CHART_LIST.CHART_ID = $chartId ",
        )
        if (!from.isNullOrEmpty()) {
            theQueryBuilder.append("AND WEEK.DATE > '").append(from).append("' ")
        }
        if (!to.isNullOrEmpty()) {
            theQueryBuilder.append("AND WEEK.DATE < '").append(to).append("' ")
        }
        theQueryBuilder.append(
            "GROUP BY TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, TRACK.ARTIST_NAME, " +
                "TRACK.FIRST_CHART_DATE, TRACK.PEAK_GLOBAL_RANK, TRACK.TOTAL_WEEKS_ON_CHART, " +
                "ARTIST._ID, ARTIST.NAME, ARTIST.NAME_NORMALIZED " +
                "ORDER BY SUM((CHART.LIST_SIZE + 1 - CHART_TRACK._RANK) * (CHART.LIST_SIZE + 1 - CHART_TRACK._RANK)) DESC ",
        )
        theQueryBuilder.append("LIMIT ").append(limit)

        return theQueryBuilder.toString()
    }

    @JvmStatic
    fun getSearchQuery(
        keywords: Array<String>,
        offset: Int,
        limit: Int,
        alphabetical: Boolean,
    ): String {
        val theQueryBuilder = StringBuilder(
            "SELECT TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, TRACK.ARTIST_NAME, " +
                "TRACK.FIRST_CHART_DATE, TRACK.PEAK_GLOBAL_RANK, TRACK.TOTAL_WEEKS_ON_CHART FROM TRACK" +
                " JOIN ARTIST ON ARTIST._ID = TRACK.ARTIST_ID",
        )
        if (!alphabetical) {
            theQueryBuilder.append(" JOIN GLOBAL_RANK_TRACK ON TRACK._ID = GLOBAL_RANK_TRACK.TRACK_ID")
        }
        var where = false
        for (keyWord in keywords) {
            if (!where) {
                theQueryBuilder.append(" WHERE")
                where = true
            } else {
                theQueryBuilder.append(" AND")
            }
            theQueryBuilder.append(" (ARTIST.NAME LIKE '%").append(keyWord.replace("'", "''")).append("%'")
                .append(" OR TRACK.TITLE LIKE '%").append(keyWord.replace("'", "''")).append("%')")
        }
        theQueryBuilder.append(" ORDER BY ").append(
            if (alphabetical) {
                "ARTIST.NAME ASC, TRACK.TITLE ASC"
            } else {
                "GLOBAL_RANK_TRACK.RANK ASC"
            },
        )
        if (limit != 0) {
            theQueryBuilder.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset)
        }
        return theQueryBuilder.toString()
    }

    @JvmStatic
    fun getCountSearchQuery(keywords: Array<String>): String {
        val theQueryBuilder = StringBuilder(
            "SELECT COUNT(TRACK._ID) FROM TRACK" +
                " JOIN ARTIST ON ARTIST._ID = TRACK.ARTIST_ID",
        )
        var where = false
        for (keyWord in keywords) {
            if (!where) {
                theQueryBuilder.append(" WHERE")
                where = true
            } else {
                theQueryBuilder.append(" AND")
            }
            theQueryBuilder.append(" (ARTIST.NAME LIKE '%").append(keyWord.replace("'", "''")).append("%'")
                .append(" OR TRACK.TITLE LIKE '%").append(keyWord.replace("'", "''")).append("%')")
        }
        return theQueryBuilder.toString()
    }

    @JvmStatic
    fun findTrack(
        tracks: List<Track>,
        trackId: Long,
    ): Track? {
        for (theTrack in tracks) {
            if (theTrack.id == trackId) {
                return theTrack
            }
        }
        return null
    }
}
