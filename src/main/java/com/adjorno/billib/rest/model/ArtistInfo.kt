package com.adjorno.billib.rest.model

import com.adjorno.billib.rest.db.Artist
import com.adjorno.billib.rest.db.Track

class ArtistInfo(
    var artist: Artist? = null,
    var globalRank: Long = 0,
    var artistRelations: List<Artist>? = null,
    var tracks: List<Track>? = null
)
