package com.ifochka.m14n.rest.model

import com.ifochka.m14n.rest.db.Artist
import com.ifochka.m14n.rest.db.Track

class ArtistInfo(
    var artist: Artist? = null,
    var globalRank: Long = 0,
    var artistRelations: List<Artist>? = null,
    var tracks: List<Track>? = null,
)
