package com.ifochka.m14n.rest.chart.domain

import com.ifochka.m14n.rest.db.Track
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ChartTrackRepository : CrudRepository<ChartTrack, Long> {
    fun findByChartList(chartList: ChartList): List<ChartTrack>

    fun findByTrackAndChartList(
        track: Track,
        chartList: ChartList,
    ): ChartTrack?

    @Query(
        value = "SELECT _RANK FROM CHART_TRACK\n" +
            "                INNER JOIN TRACK ON TRACK._ID = CHART_TRACK.TRACK_ID\n" +
            "                INNER JOIN CHART_LIST ON  CHART_TRACK.CHART_LIST_ID = CHART_LIST._ID\n" +
            "                WHERE CHART_TRACK.CHART_LIST_ID IN (\n" +
            "                SELECT CHART_LIST.PREVIOUS_CHART_LIST_ID FROM CHART_LIST\n" +
            "                INNER JOIN CHART_TRACK ON  CHART_TRACK.CHART_LIST_ID = CHART_LIST._ID\n" +
            "                WHERE CHART_TRACK._ID = :id)\n" + "                AND TRACK_ID IN (\n" +
            "                SELECT TRACK_ID FROM CHART_TRACK\n" +
            "                WHERE CHART_TRACK._ID = :id)",
        nativeQuery = true,
    )
    fun findPreviousWeekRank(
        @Param(value = "id") mChartTrackId: Long,
    ): List<Int>

    @Query(
        value =
            "select ct, (c.listSize - ct.rank)*(c.listSize - ct.rank) - c.listSize as rating from ChartTrack ct " +
                "inner join ct.chartList cl inner join cl.week w inner join cl.chart c " +
                "where w.id = ?1 and ct.lastWeekRank = 0",
    )
    fun findDebuts(weekId: Long): List<Array<Any>>

    @Query(value = "select ct from ChartTrack ct inner join ct.chartList cl where cl.id = ?1 and ct.rank = ?2")
    fun findByChartListIdAndRank(
        id: Long,
        rank: Int,
    ): List<ChartTrack>

    @Modifying
    @Query(value = "update ChartTrack ct set ct.track = ?2 where ct.track = ?1")
    fun updateTracks(
        from: Track,
        to: Track,
    )

    @Modifying
    @Query(value = "update ChartTrack ct set ct.rank = ?2 where ct = ?1")
    fun updateRank(
        sameTrack: ChartTrack,
        rank: Int,
    )

    @Modifying
    @Query(value = "update ChartTrack ct set ct.lastWeekRank = ?2 where ct = ?1")
    fun updateLastWeekRank(
        sameTrack: ChartTrack,
        lastWeekRank: Int,
    )

    @Modifying
    @Query(value = "update ChartTrack ct set ct.track = ?2 where ct = ?1")
    fun updateTrack(
        chartTrack: ChartTrack,
        track: Track,
    )

    @Query(value = "select ct from ChartTrack ct where ct.chartList = ?1")
    fun countRealChartListSize(previousChartList: ChartList): List<ChartTrack>

    @Query(value = "select ct from ChartTrack ct inner join ct.chartList cl inner join cl.week w where ct.track = ?1")
    fun findByTrackAndSort(
        track: Track,
        sort: Sort,
    ): List<ChartTrack>

    @Query(value = "select ct from ChartTrack ct inner join ct.chartList cl where cl.chart = ?2 and ct.track = ?1")
    fun findByTrackAndChart(
        chart: Chart,
        track: Track,
    ): List<ChartTrack>

    fun findByTrack(track: Track): List<ChartTrack>

    @Query(value = "select ct from ChartTrack ct inner join ct.chartList cl where cl.chart in ?2 and ct.track = ?1")
    fun findByTrackInCharts(
        track: Track,
        charts: Iterable<Chart>,
    ): List<ChartTrack>

    @Query(value = "select ct from ChartTrack ct inner join ct.chartList cl where cl.week = ?1")
    fun findByWeek(week: Week): List<ChartTrack>
}
