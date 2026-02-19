package com.ifochka.m14n.rest

import jakarta.persistence.EntityManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for database maintenance operations.
 * Provides endpoints to refresh global rankings for tracks and artists.
 */
@RestController
class UpdateController(
    private val entityManager: EntityManager,
) {
    /**
     * Refreshes global rankings for both tracks and artists.
     * This updates the materialized views that calculate rankings based on chart performance.
     *
     * Formula: SUM((list_size - rank) * (list_size - rank)) for all chart appearances
     * Higher score = better ranking
     *
     * Usage: POST /updateGlobal
     */
    @RequestMapping(value = ["/updateGlobal"], method = [RequestMethod.POST])
    fun updateGlobalAPI(): UpdateGlobalResult =
        try {
            val tracksUpdated = updateGlobalRankingTracks()
            val artistsUpdated = updateGlobalRankingArtists()

            UpdateGlobalResult(
                success = true,
                message = "Global rankings refreshed successfully",
                tracksUpdated = tracksUpdated,
                artistsUpdated = artistsUpdated,
            )
        } catch (e: Exception) {
            UpdateGlobalResult(
                success = false,
                message = "Error refreshing global rankings: ${e.message}",
            )
        }

    /**
     * Refreshes global track rankings materialized view.
     * This recalculates rankings for all tracks based on their chart performance.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    fun updateGlobalRankingTracks(): Int {
        println("STARTED UPDATE GLOBAL TRACK RANKINGS")

        // Call the database function to refresh the materialized view
        entityManager.createNativeQuery("SELECT refresh_global_rankings()")
            .singleResult

        // Get count of ranked tracks
        val count = entityManager.createNativeQuery("SELECT COUNT(*) FROM GLOBAL_RANK_TRACK")
            .singleResult as Long

        println("FINISHED UPDATE GLOBAL TRACK RANKINGS - $count tracks ranked")
        return count.toInt()
    }

    /**
     * Refreshes global artist rankings materialized view.
     * This recalculates rankings for all artists based on their chart performance.
     * Note: This is called as part of updateGlobalRankingTracks via the database function.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    fun updateGlobalRankingArtists(): Int {
        println("GETTING GLOBAL ARTIST RANKINGS COUNT")

        // Get count of ranked artists
        val count = entityManager.createNativeQuery("SELECT COUNT(*) FROM GLOBAL_RANK_ARTIST")
            .singleResult as Long

        println("GLOBAL ARTIST RANKINGS - $count artists ranked")
        return count.toInt()
    }

    /**
     * Refreshes both global rankings using the database helper function.
     * This is a convenience method that calls the PostgreSQL function.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    fun refreshGlobalRankingsUsingFunction() {
        println("CALLING refresh_global_rankings() FUNCTION")
        entityManager.createNativeQuery("SELECT refresh_global_rankings()")
            .singleResult
        println("FINISHED refresh_global_rankings() FUNCTION")
    }
}

/**
 * Result of global ranking update operation.
 */
data class UpdateGlobalResult(
    val success: Boolean,
    val message: String,
    val tracksUpdated: Int? = null,
    val artistsUpdated: Int? = null,
)
