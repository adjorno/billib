package com.ifochka.m14n.rest.catalog.rest.dtos

import com.ifochka.m14n.rest.catalog.domain.Track

data class TrackInfo(
    var track: Track? = null,
    var history: Map<String, Map<String, Int>>? = null,
    var globalRank: Long = 0,
)
