package com.adjorno.billib.rest

import com.adjorno.billib.rest.db.Artist
import com.adjorno.billib.rest.db.Track

interface ITrackController {
    fun getTracks(artist: Artist, size: Int): List<Track>

    fun getTrackHistory(id: Long, chartId: Long?): Map<String, Map<String, Int>>

    fun updateDayTrack(formattedDay: String): Track
}
