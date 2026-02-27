package com.ifochka.m14n.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ArtistInfo(
    val artist: Artist,
    val globalRank: Long = 0,
    val artistRelations: List<Artist>? = null,
    val tracks: List<Track>? = null,
)
