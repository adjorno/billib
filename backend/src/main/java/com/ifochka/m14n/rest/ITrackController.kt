package com.ifochka.m14n.rest

import com.ifochka.m14n.rest.db.Artist
import com.ifochka.m14n.rest.db.Track

interface ITrackController {
    fun getTracks(
        artist: Artist,
        size: Int,
    ): List<Track>

    fun getTrackHistory(
        id: Long,
        chartId: Long?,
    ): Map<String, Map<String, Int>>

    fun updateDayTrack(formattedDay: String): Track
}
