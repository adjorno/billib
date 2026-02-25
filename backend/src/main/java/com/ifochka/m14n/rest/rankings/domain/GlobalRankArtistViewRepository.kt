package com.ifochka.m14n.rest.rankings.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GlobalRankArtistViewRepository : JpaRepository<GlobalRankArtistView, Long> {
    fun findByArtistId(artistId: Long): GlobalRankArtistView?

    @Query(
        """
        SELECT gra FROM GlobalRankArtistView gra
        ORDER BY gra.rank
    """,
    )
    fun findTopRanked(pageable: Pageable): Page<GlobalRankArtistView>

    @Query(
        """
        SELECT gra FROM GlobalRankArtistView gra
        WHERE gra.rank BETWEEN :minRank AND :maxRank
        ORDER BY gra.rank
    """,
    )
    fun findByRankRange(
        @Param("minRank") minRank: Long,
        @Param("maxRank") maxRank: Long,
    ): List<GlobalRankArtistView>

    @Query(
        """
        SELECT gra FROM GlobalRankArtistView gra
        WHERE gra.uniqueTracks >= :minTracks
        ORDER BY gra.rank
    """,
    )
    fun findByMinimumTracks(
        @Param("minTracks") minTracks: Long,
        pageable: Pageable,
    ): Page<GlobalRankArtistView>
}
