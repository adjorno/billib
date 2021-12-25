package com.adjorno.billib.rest.model

import com.adjorno.billib.rest.db.Artist
import com.adjorno.billib.rest.db.Track

class MergedSearchResult {
    var artists: SearchResult<Artist>? = null
    var tracks: SearchResult<Track>? = null
}
