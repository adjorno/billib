package com.ifochka.m14n.data.artwork

import kotlinx.serialization.Serializable

@Serializable
data class AppleMusicSearchResponse(
    val results: AppleMusicResults,
)

@Serializable
data class AppleMusicResults(
    val songs: AppleMusicCollection<AppleMusicSongAttributes>? = null,
    val artists: AppleMusicCollection<AppleMusicArtistAttributes>? = null,
)

@Serializable
data class AppleMusicCollection<T>(
    val data: List<AppleMusicResource<T>> = emptyList(),
)

@Serializable
data class AppleMusicResource<T>(
    val id: String,
    val attributes: T? = null,
)

@Serializable
data class AppleMusicSongAttributes(
    val artwork: AppleMusicArtwork? = null,
)

@Serializable
data class AppleMusicArtistAttributes(
    val artwork: AppleMusicArtwork? = null,
)

@Serializable
data class AppleMusicArtwork(
    val url: String? = null,
)
