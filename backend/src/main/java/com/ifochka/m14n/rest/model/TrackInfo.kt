package com.ifochka.m14n.rest.model

import com.ifochka.m14n.rest.db.Track

data class TrackInfo(
    var track: Track? = null,
    var history: Map<String, Map<String, Int>>? = null,
    var globalRank: Long = 0,
)
