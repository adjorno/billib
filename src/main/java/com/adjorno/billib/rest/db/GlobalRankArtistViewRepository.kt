package com.adjorno.billib.rest.db

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GlobalRankArtistViewRepository : JpaRepository<GlobalRankArtistView, Long> {

    /**
     * Find global rank for a specific artist
     */
    fun findByArtistId(artistId: Long): GlobalRankArtistView?

    /**
     * Get top ranked artists
     */
    @Query("""
        SELECT gra FROM GlobalRankArtistView gra
        ORDER BY gra.rank
    """)
    fun findTopRanked(pageable: Pageable): Page<GlobalRankArtistView>

    /**
     * Get artists with rank range
     */
    @Query("""
        SELECT gra FROM GlobalRankArtistView gra
        WHERE gra.rank BETWEEN :minRank AND :maxRank
        ORDER BY gra.rank
    """)
    fun findByRankRange(
        @Param("minRank") minRank: Long,
        @Param("maxRank") maxRank: Long
    ): List<GlobalRankArtistView>

    /**
     * Get artists with minimum track count
     */
    @Query("""
        SELECT gra FROM GlobalRankArtistView gra
        WHERE gra.uniqueTracks >= :minTracks
        ORDER BY gra.rank
    """)
    fun findByMinimumTracks(
        @Param("minTracks") minTracks: Long,
        pageable: Pageable
    ): Page<GlobalRankArtistView>
}
