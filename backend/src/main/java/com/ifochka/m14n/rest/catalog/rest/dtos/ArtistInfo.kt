package com.ifochka.m14n.rest.catalog.rest.dtos

import com.ifochka.m14n.rest.catalog.domain.Artist
import com.ifochka.m14n.rest.catalog.domain.Track

class ArtistInfo(
    var artist: Artist? = null,
    var globalRank: Long = 0,
    var artistRelations: List<Artist>? = null,
    var tracks: List<Track>? = null,
)
