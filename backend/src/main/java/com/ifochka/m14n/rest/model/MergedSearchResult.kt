package com.ifochka.m14n.rest.model

import com.ifochka.m14n.rest.db.Artist
import com.ifochka.m14n.rest.db.Track

class MergedSearchResult {
    var artists: SearchResult<Artist>? = null
    var tracks: SearchResult<Track>? = null
}
