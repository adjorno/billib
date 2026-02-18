package com.ifochka.billib.data.artwork

import com.ifochka.billib.data.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cached artwork repository that fetches from iTunes API.
 *
 * Note: Database caching will be implemented in a future iteration.
 * Currently fetches directly from iTunes API with in-memory caching via Track.artworkUrl.
 */
class CachedArtworkRepository(
    private val artworkApi: ArtworkApi,
    private val requestDelayMs: Long = 1000L,
) : ArtworkRepository {
    private val rateLimitMutex = Mutex()

    @Suppress("TooGenericExceptionCaught") // Graceful degradation requires catching all exceptions
    override suspend fun getArtworkUrl(track: Track): String? {
        println("[ARTWORK-REPO] 🔍 getArtworkUrl called for: ${track.artistName} - ${track.title}")

        // Check in-memory cache first
        val cachedUrl = track.artworkUrl?.takeIf { it.isNotBlank() }
        if (cachedUrl != null) {
            println("[ARTWORK-REPO] ⊘ Track already has artwork: $cachedUrl")
            return cachedUrl
        }

        // Extract and validate required fields
        val artistName = track.artistName ?: track.artist?.name
        val trackTitle = track.title
        val result = when {
            artistName == null || trackTitle == null -> {
                println("[ARTWORK-REPO] ❌ Missing required fields - artist: $artistName, title: $trackTitle")
                null
            }
            else -> fetchArtworkFromApi(artistName, trackTitle)
        }

        return result
    }

    /**
     * Fetch artwork from iTunes API with error handling.
     */
    @Suppress("TooGenericExceptionCaught") // Need to catch all exceptions for graceful degradation
    private suspend fun fetchArtworkFromApi(
        artistName: String,
        trackTitle: String,
    ): String? =
        rateLimitMutex.withLock {
            println("[ARTWORK-REPO] 📡 Calling iTunes API for: '$artistName' - '$trackTitle'")
            try {
                val result = artworkApi.searchArtwork(
                    artist = artistName,
                    title = trackTitle,
                )
                println("[ARTWORK-REPO] ✓ iTunes API returned: $result")
                result
            } catch (e: Exception) {
                println("[ARTWORK-REPO] ❌ Failed to fetch artwork for '$artistName - $trackTitle': ${e.message}")
                println("[ARTWORK-REPO] ❌ Exception: ${e::class.simpleName}")
                null
            } finally {
                delay(requestDelayMs)
            }
        }

    override suspend fun getArtworkUrls(tracks: List<Track>): Map<Long, String?> {
        val artworkMap = mutableMapOf<Long, String?>()

        tracks.forEach { track ->
            val trackId = track.id ?: return@forEach

            // Skip if already have artwork
            if (!track.artworkUrl.isNullOrBlank()) {
                artworkMap[trackId] = track.artworkUrl
                return@forEach
            }

            // Fetch artwork
            val artworkUrl = getArtworkUrl(track)
            artworkMap[trackId] = artworkUrl

            if (artworkUrl != null) {
                println("[ARTWORK] ✓ Fetched artwork for: ${track.artistName} - ${track.title}")
            }
        }

        return artworkMap
    }
}
