package com.ifochka.m14n.data.artwork

interface ArtworkUrlPersistence {
    suspend fun getArtworkUrl(trackId: Long): String?

    suspend fun saveArtworkUrl(
        trackId: Long,
        url: String,
    )
}
