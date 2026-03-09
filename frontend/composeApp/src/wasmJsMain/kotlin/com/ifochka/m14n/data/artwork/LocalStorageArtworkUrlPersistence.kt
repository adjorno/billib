package com.ifochka.m14n.data.artwork

private external fun jsLocalStorageGet(key: String): String?

private external fun jsLocalStorageSet(
    key: String,
    value: String,
)

class LocalStorageArtworkUrlPersistence : ArtworkUrlPersistence {
    override suspend fun getArtworkUrl(trackId: Long): String? {
        val url = jsLocalStorageGet("artwork_track_$trackId")
        // if (url != null) {
        //     println("[ARTWORK-PERSISTENCE] ✓ Hit localStorage for track $trackId: $url")
        // } else {
        //     println("[ARTWORK-PERSISTENCE] ✗ Miss for track $trackId — calling iTunes API")
        // }
        return url
    }

    override suspend fun saveArtworkUrl(
        trackId: Long,
        url: String,
    ) {
        jsLocalStorageSet("artwork_track_$trackId", url)
        // println("[ARTWORK-PERSISTENCE] ✓ Saved to localStorage for track $trackId")
    }
}
