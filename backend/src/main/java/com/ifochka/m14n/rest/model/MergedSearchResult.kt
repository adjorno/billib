package com.ifochka.m14n.rest.model

import com.ifochka.m14n.rest.catalog.domain.Artist
import com.ifochka.m14n.rest.catalog.domain.Track

class MergedSearchResult {
    var artists: SearchResult<Artist>? = null
    var tracks: SearchResult<Track>? = null
}
