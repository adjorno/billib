package com.ifochka.m14n.data.artwork

import com.ifochka.m14n.LogFlags
import com.ifochka.m14n.data.model.Artist
import com.ifochka.m14n.data.model.Track

class CachedArtworkRepository(
    private val artworkApi: ArtworkApi,
    private val persistence: ArtworkUrlPersistence,
) : ArtworkRepository {
    private val artistArtworkCache = mutableMapOf<Long, String>()

    @Suppress("TooGenericExceptionCaught") // Graceful degradation requires catching all exceptions
    override suspend fun getArtworkUrl(track: Track): String? {
        if (LogFlags.ARTWORK_REPO) {
            println(
                "[ARTWORK-REPO] 🔍 getArtworkUrl called for: ${track.artistName} - ${track.title}",
            )
        }

        // Check in-memory cache first (populated by DB JOIN on native platforms)
        val cachedUrl = track.artworkUrl?.takeIf { it.isNotBlank() }
        if (cachedUrl != null) {
            if (LogFlags.ARTWORK_REPO) println("[ARTWORK-REPO] ⊘ Track already has artwork: $cachedUrl")
            return cachedUrl
        }

        return fetchWithPersistence(track)
    }

    private suspend fun fetchWithPersistence(track: Track): String? {
        val trackId = track.id

        // Check persistent storage (localStorage on web, SQLite on native)
        if (trackId != null) {
            val persistedUrl = persistence.getArtworkUrl(trackId)
            if (persistedUrl != null) return persistedUrl
        }

        // Extract and validate required fields
        val artistName = track.artistName ?: track.artist?.name
        val trackTitle = track.title
        val result = when {
            artistName == null || trackTitle == null -> {
                if (LogFlags.ARTWORK_REPO) {
                    println(
                        "[ARTWORK-REPO] ❌ Missing required fields - artist: $artistName, title: $trackTitle",
                    )
                }
                null
            }
            else -> fetchArtworkFromApi(artistName, trackTitle)
        }

        // Persist the fetched URL for future loads
        if (result != null && trackId != null) {
            persistence.saveArtworkUrl(trackId = trackId, url = result)
        }

        return result
    }

    // Need to catch all exceptions for graceful degradation
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun fetchArtworkFromApi(
        artistName: String,
        trackTitle: String,
    ): String? =
        try {
            artworkApi.searchArtwork(
                artist = artistName,
                title = trackTitle,
            )
        } catch (e: Exception) {
            if (LogFlags.ARTWORK_REPO) {
                println(
                    "[ARTWORK-REPO] ❌ Failed to fetch artwork for '$artistName - $trackTitle': ${e.message}",
                )
            }
            if (LogFlags.ARTWORK_REPO) println("[ARTWORK-REPO] ❌ Exception: ${e::class.simpleName}")
            null
        }

    @Suppress("TooGenericExceptionCaught") // Graceful degradation requires catching all exceptions
    override suspend fun getArtworkUrlForArtist(artist: Artist): String? {
        val id = artist.id ?: return null
        return artistArtworkCache[id] ?: artist.name?.let { name ->
            val url = runCatching { artworkApi.searchArtistArtwork(name) }.getOrNull()
            if (url != null) artistArtworkCache[id] = url
            url
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
                if (LogFlags.ARTWORK_REPO) {
                    println(
                        "[ARTWORK] ✓ Fetched artwork for: ${track.artistName} - ${track.title}",
                    )
                }
            }
        }

        return artworkMap
    }
}
