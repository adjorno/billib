package com.ifochka.m14n.data.artwork

import com.ifochka.m14n.data.db.ChartDatabaseRepository

class SqlDelightArtworkUrlPersistence(
    private val database: ChartDatabaseRepository,
) : ArtworkUrlPersistence {
    override suspend fun getArtworkUrl(trackId: Long): String? {
        val url = database.getArtworkUrl(trackId)
        if (url != null) {
            println("[ARTWORK-PERSISTENCE] ✓ Hit SQLite for track $trackId: $url")
        } else {
            println("[ARTWORK-PERSISTENCE] ✗ Miss for track $trackId — calling iTunes API")
        }
        return url
    }

    override suspend fun saveArtworkUrl(
        trackId: Long,
        url: String,
    ) {
        database.updateTrackArtwork(trackId = trackId, artworkUrl = url)
        println("[ARTWORK-PERSISTENCE] ✓ Saved to SQLite for track $trackId")
    }
}
