package com.ifochka.m14n.data.artwork

/**
 * Interface for fetching album artwork URLs from external APIs.
 */
interface ArtworkApi {
    /**
     * Search for album artwork by artist and track title.
     *
     * @param artist The artist name
     * @param title The track title
     * @return The artwork URL if found, null otherwise
     */
    suspend fun searchArtwork(
        artist: String,
        title: String,
    ): String?
}
