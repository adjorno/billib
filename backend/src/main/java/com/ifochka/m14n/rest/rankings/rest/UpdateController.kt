package com.ifochka.m14n.rest.rankings.rest

import jakarta.persistence.EntityManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class UpdateController(
    private val entityManager: EntityManager,
) {
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

    @Transactional(propagation = Propagation.REQUIRED)
    fun updateGlobalRankingTracks(): Int {
        println("STARTED UPDATE GLOBAL TRACK RANKINGS")
        entityManager.createNativeQuery("SELECT refresh_global_rankings()")
            .singleResult
        val count = entityManager.createNativeQuery("SELECT COUNT(*) FROM GLOBAL_RANK_TRACK")
            .singleResult as Long
        println("FINISHED UPDATE GLOBAL TRACK RANKINGS - $count tracks ranked")
        return count.toInt()
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun updateGlobalRankingArtists(): Int {
        println("GETTING GLOBAL ARTIST RANKINGS COUNT")
        val count = entityManager.createNativeQuery("SELECT COUNT(*) FROM GLOBAL_RANK_ARTIST")
            .singleResult as Long
        println("GLOBAL ARTIST RANKINGS - $count artists ranked")
        return count.toInt()
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun refreshGlobalRankingsUsingFunction() {
        println("CALLING refresh_global_rankings() FUNCTION")
        entityManager.createNativeQuery("SELECT refresh_global_rankings()")
            .singleResult
        println("FINISHED refresh_global_rankings() FUNCTION")
    }
}

data class UpdateGlobalResult(
    val success: Boolean,
    val message: String,
    val tracksUpdated: Int? = null,
    val artistsUpdated: Int? = null,
)
