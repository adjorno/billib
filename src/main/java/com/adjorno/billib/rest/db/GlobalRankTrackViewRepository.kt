package com.adjorno.billib.rest.db

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GlobalRankTrackViewRepository : JpaRepository<GlobalRankTrackView, Long> {

    /**
     * Find global rank for a specific track
     */
    fun findByTrackId(trackId: Long): GlobalRankTrackView?

    /**
     * Get top ranked tracks
     */
    @Query("""
        SELECT grt FROM GlobalRankTrackView grt
        ORDER BY grt.rank
    """)
    fun findTopRanked(pageable: Pageable): Page<GlobalRankTrackView>

    /**
     * Get tracks with rank range
     */
    @Query("""
        SELECT grt FROM GlobalRankTrackView grt
        WHERE grt.rank BETWEEN :minRank AND :maxRank
        ORDER BY grt.rank
    """)
    fun findByRankRange(
        @Param("minRank") minRank: Long,
        @Param("maxRank") maxRank: Long
    ): List<GlobalRankTrackView>

    /**
     * Get tracks with minimum appearances
     */
    @Query("""
        SELECT grt FROM GlobalRankTrackView grt
        WHERE grt.totalAppearances >= :minAppearances
        ORDER BY grt.rank
    """)
    fun findByMinimumAppearances(
        @Param("minAppearances") minAppearances: Long,
        pageable: Pageable
    ): Page<GlobalRankTrackView>
}
