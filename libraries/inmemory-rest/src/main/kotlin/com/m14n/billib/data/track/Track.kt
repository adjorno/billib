package com.m14n.billib.data.track

import com.m14n.billib.data.artist.Artist
import com.m14n.billib.data.dao.HasId
import com.m14n.billib.data.dao.HasName
import kotlinx.serialization.Serializable

/**
 * Track data primitive
 */
@Serializable
data class Track(
    override val id: Long,
    val title: String,
    val artist: Artist
) : HasId,
    HasName {
    override val name: String
        get() = (artist.name to title).fullTrackTitle
}

val Pair<String, String>.fullTrackTitle: String
    get() = "$first - $second"

