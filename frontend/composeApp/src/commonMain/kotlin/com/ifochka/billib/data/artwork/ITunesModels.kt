package com.ifochka.billib.data.artwork

import kotlinx.serialization.Serializable

/**
 * iTunes Search API response model.
 *
 * API Documentation: https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/
 */
@Serializable
data class ITunesSearchResponse(
    val resultCount: Int,
    val results: List<ITunesTrack>,
)

/**
 * iTunes track result containing artwork URLs.
 */
@Serializable
data class ITunesTrack(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val artworkUrl30: String? = null,
    val artworkUrl60: String? = null,
    val artworkUrl100: String? = null,
)
