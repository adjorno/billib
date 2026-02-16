package com.ifochka.billib.rest.model

import com.ifochka.billib.rest.db.Artist
import com.ifochka.billib.rest.db.Track

class MergedSearchResult {
    var artists: SearchResult<Artist>? = null
    var tracks: SearchResult<Track>? = null
}
