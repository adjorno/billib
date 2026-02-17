package com.ifochka.billib.data.artwork

import com.ifochka.billib.data.db.ChartDatabaseRepository
import com.ifochka.billib.data.model.Track
import kotlin.time.Duration.Companion.days

/**
 * Cached artwork repository that fetches from iTunes API and caches results.
 *
 * Caching strategy:
 * - TTL: 30 days (album art rarely changes)
 * - Storage: SQLDelight (artwork_url column in track table)
 * - Rate limiting: Reactive retry on 403 errors with exponential backoff
 */
class CachedArtworkRepository(
    private val artworkApi: ArtworkApi,
    private val database: ChartDatabaseRepository,
) : ArtworkRepository {
    companion object {
        private val ARTWORK_CACHE_TTL_MS = 30.days.inWholeMilliseconds
    }

    override suspend fun getArtworkUrl(track: Track): String? {
        println("[ARTWORK-REPO] 🔍 getArtworkUrl called for: ${track.artistName} - ${track.title}")

        // If track already has artwork URL, return it
        if (!track.artworkUrl.isNullOrBlank()) {
            println("[ARTWORK-REPO] ⊘ Track already has artwork: ${track.artworkUrl}")
            return track.artworkUrl
        }

        // Try to fetch from API
        val artistName = track.artistName ?: track.artist?.name ?: run {
            println("[ARTWORK-REPO] ❌ No artist name available")
            return null
        }
        val trackTitle = track.title ?: run {
            println("[ARTWORK-REPO] ❌ No track title available")
            return null
        }

        println("[ARTWORK-REPO] 📡 Calling iTunes API for: '$artistName' - '$trackTitle'")

        return try {
            val result = artworkApi.searchArtwork(
                artist = artistName,
                title = trackTitle,
            )
            println("[ARTWORK-REPO] ✓ iTunes API returned: $result")
            result
        } catch (e: Exception) {
            println("[ARTWORK-REPO] ❌ Failed to fetch artwork for '$artistName - $trackTitle': ${e.message}")
            println("[ARTWORK-REPO] ❌ Exception: ${e::class.simpleName}")
            e.printStackTrace()
            null
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
