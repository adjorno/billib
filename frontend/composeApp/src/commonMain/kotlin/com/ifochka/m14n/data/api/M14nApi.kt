package com.ifochka.m14n.data.api

import com.ifochka.m14n.data.model.ArtistInfo
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList
import com.ifochka.m14n.data.model.DayTrack
import com.ifochka.m14n.data.model.MergedSearchResult
import com.ifochka.m14n.data.model.Track
import com.ifochka.m14n.data.model.TrackInfo
import com.ifochka.m14n.data.model.Trends

interface M14nApi {
    suspend fun getAllCharts(): Result<List<Chart>>

    suspend fun getChartByDate(
        chartId: Long,
        date: String? = null,
    ): Result<ChartList>

    suspend fun getTrends(date: String? = null): Result<Trends>

    suspend fun getDayTrack(date: String? = null): Result<DayTrack>

    suspend fun getBestTracks(
        chartId: Long,
        from: String? = null,
        to: String? = null,
        size: Int = 100,
    ): Result<List<Track>>

    suspend fun search(
        query: String,
        artistsSize: Int = 5,
        tracksSize: Int = 10,
    ): Result<MergedSearchResult>

    suspend fun getTrackById(trackId: Long): Result<Track>

    suspend fun getTrackInfo(trackId: Long): Result<TrackInfo>

    suspend fun getArtistInfo(artistId: Long): Result<ArtistInfo>

    suspend fun syncUser(): Result<Unit>
}
