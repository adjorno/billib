package com.ifochka.m14n.rest.catalog.rest

import com.ifochka.m14n.rest.catalog.domain.Artist
import com.ifochka.m14n.rest.catalog.domain.Track

interface ITrackController {
    fun getTracks(
        artist: Artist,
        size: Int,
    ): List<Track>

    fun getTrackHistory(
        id: Long,
        chartId: Long?,
    ): Map<String, Map<String, Int>>
}
