package com.ifochka.billib.data.artwork

import com.ifochka.billib.data.model.Track

/**
 * Repository for managing album artwork URLs.
 */
interface ArtworkRepository {
    /**
     * Fetch artwork URL for a track, using cache when available.
     *
     * @param track The track to fetch artwork for
     * @return The artwork URL if found, null otherwise
     */
    suspend fun getArtworkUrl(track: Track): String?

    /**
     * Fetch artwork URLs for multiple tracks in batch.
     *
     * @param tracks The list of tracks to fetch artwork for
     * @return Map of track ID to artwork URL
     */
    suspend fun getArtworkUrls(tracks: List<Track>): Map<Long, String?>
}
