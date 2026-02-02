package com.adjorno.billib.rest.db

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ChartTrackPositionRepository : JpaRepository<ChartTrackPosition, Long> {

    /**
     * Get track history across all charts, ordered by most recent first
     * Leverages partition pruning and idx_ctp_track_date index
     */
    @Query("""
        SELECT ctp FROM ChartTrackPosition ctp
        WHERE ctp.trackId = :trackId
        ORDER BY ctp.weekDate DESC
    """)
    fun findTrackHistory(
        @Param("trackId") trackId: Long,
        pageable: Pageable
    ): Page<ChartTrackPosition>

    /**
     * Get track history for a specific chart
     */
    @Query("""
        SELECT ctp FROM ChartTrackPosition ctp
        WHERE ctp.trackId = :trackId
        AND ctp.chartId = :chartId
        ORDER BY ctp.weekDate DESC
    """)
    fun findTrackHistoryByChart(
        @Param("trackId") trackId: Long,
        @Param("chartId") chartId: Long,
        pageable: Pageable
    ): Page<ChartTrackPosition>

    /**
     * Get all chart positions for a specific week
     */
    @Query("""
        SELECT ctp FROM ChartTrackPosition ctp
        WHERE ctp.weekDate = :weekDate
        ORDER BY ctp.chartId, ctp.rank
    """)
    fun findByWeekDate(
        @Param("weekDate") weekDate: LocalDate
    ): List<ChartTrackPosition>

    /**
     * Get chart positions for a specific chart and week
     */
    @Query("""
        SELECT ctp FROM ChartTrackPosition ctp
        WHERE ctp.chartId = :chartId
        AND ctp.weekDate = :weekDate
        ORDER BY ctp.rank
    """)
    fun findByChartAndWeek(
        @Param("chartId") chartId: Long,
        @Param("weekDate") weekDate: LocalDate
    ): List<ChartTrackPosition>

    /**
     * Get all debuts for a specific week
     * Uses partial index on IS_DEBUT
     */
    @Query("""
        SELECT ctp FROM ChartTrackPosition ctp
        WHERE ctp.weekDate = :weekDate
        AND ctp.isDebut = true
        ORDER BY ctp.chartId, ctp.rank
    """)
    fun findDebutsByWeek(
        @Param("weekDate") weekDate: LocalDate
    ): List<ChartTrackPosition>

    /**
     * Get artist's chart positions within a date range
     * Leverages idx_ctp_artist_date index
     */
    @Query("""
        SELECT ctp FROM ChartTrackPosition ctp
        WHERE ctp.artistId = :artistId
        AND ctp.weekDate BETWEEN :startDate AND :endDate
        ORDER BY ctp.weekDate DESC, ctp.rank
    """)
    fun findByArtistAndDateRange(
        @Param("artistId") artistId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<ChartTrackPosition>

    /**
     * Count total appearances for a track
     */
    @Query("""
        SELECT COUNT(ctp) FROM ChartTrackPosition ctp
        WHERE ctp.trackId = :trackId
    """)
    fun countByTrackId(@Param("trackId") trackId: Long): Long

    /**
     * Get peak position for a track across all charts
     */
    @Query("""
        SELECT MIN(ctp.rank) FROM ChartTrackPosition ctp
        WHERE ctp.trackId = :trackId
    """)
    fun findPeakPositionByTrackId(@Param("trackId") trackId: Long): Int?
}
