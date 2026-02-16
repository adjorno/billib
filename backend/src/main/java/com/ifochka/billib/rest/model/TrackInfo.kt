package com.ifochka.billib.rest.model

import com.ifochka.billib.rest.db.Track

data class TrackInfo(
    var track: Track? = null,
    var history: Map<String, Map<String, Int>>? = null,
    var globalRank: Long = 0,
)
