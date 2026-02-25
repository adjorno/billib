package com.ifochka.m14n.rest.rankings.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GlobalRankTrackViewRepository : JpaRepository<GlobalRankTrackView, Long> {
    fun findByTrackId(trackId: Long): GlobalRankTrackView?

    @Query(
        """
        SELECT grt FROM GlobalRankTrackView grt
        ORDER BY grt.rank
    """,
    )
    fun findTopRanked(pageable: Pageable): Page<GlobalRankTrackView>

    @Query(
        """
        SELECT grt FROM GlobalRankTrackView grt
        WHERE grt.rank BETWEEN :minRank AND :maxRank
        ORDER BY grt.rank
    """,
    )
    fun findByRankRange(
        @Param("minRank") minRank: Long,
        @Param("maxRank") maxRank: Long,
    ): List<GlobalRankTrackView>

    @Query(
        """
        SELECT grt FROM GlobalRankTrackView grt
        WHERE grt.totalAppearances >= :minAppearances
        ORDER BY grt.rank
    """,
    )
    fun findByMinimumAppearances(
        @Param("minAppearances") minAppearances: Long,
        pageable: Pageable,
    ): Page<GlobalRankTrackView>
}
