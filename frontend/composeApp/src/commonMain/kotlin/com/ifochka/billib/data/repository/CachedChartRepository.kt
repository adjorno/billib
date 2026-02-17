package com.ifochka.billib.data.repository

import com.ifochka.billib.data.artwork.ArtworkRepository
import com.ifochka.billib.data.db.CachePolicy
import com.ifochka.billib.data.db.ChartDatabaseRepository
import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CachedChartRepository(
    private val database: ChartDatabaseRepository,
    private val network: NetworkChartRepository,
    private val artworkRepository: ArtworkRepository,
) {
    // Background scope for non-blocking artwork loading
    private val artworkScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Flow that emits when a track's artwork is updated
    private val _chartUpdates = MutableSharedFlow<ChartList>(replay = 0)
    val chartUpdates: SharedFlow<ChartList> = _chartUpdates.asSharedFlow()
    suspend fun getAllCharts(): Result<List<Chart>> {
        val cachedCharts = database.getAllCharts()

        if (cachedCharts.isNotEmpty()) {
            val count = cachedCharts.size
            if (count <= CachePolicy.MAX_CHARTS_COUNT) {
                println("[CACHE] ✓ Charts loaded from cache ($count charts)")
                return Result.success(cachedCharts)
            }
        }

        println("[CACHE] ↓ Fetching charts from network...")
        return network.getAllCharts().onSuccess { charts ->
            val limitedCharts = charts.take(CachePolicy.MAX_CHARTS_COUNT)
            database.clearCharts()
            database.insertCharts(limitedCharts)
            println("[CACHE] ✓ Cached ${limitedCharts.size} charts (capped at ${CachePolicy.MAX_CHARTS_COUNT})")
        }.recoverCatching { networkError ->
            if (cachedCharts.isNotEmpty()) {
                println("[CACHE] ⚠ Network failed, using stale cache (${cachedCharts.size} charts)")
                cachedCharts
            } else {
                println("[CACHE] ✗ Network failed, no cache available")
                throw networkError
            }
        }
    }

    suspend fun getChartByDate(
        chartId: Long,
        date: String?,
    ): Result<ChartList> {
        val effectiveDate = date ?: "latest"

        val cachedTimestamp = database.getChartListCachedAt(chartId, effectiveDate)
        val cachedChartList = cachedTimestamp?.let {
            val isStale = CachePolicy.isCacheStale(it, CachePolicy.CACHE_TTL_MS)
            if (!isStale) {
                database.getChartListByDate(chartId, effectiveDate)
            } else {
                println("[CACHE] ⏱ Chart list cache is stale (chart=$chartId, date=$effectiveDate)")
                null
            }
        }

        if (cachedChartList != null) {
            println("[CACHE] ✓ Chart list loaded from cache (chart=$chartId, date=$effectiveDate, ttl=7d)")
            return Result.success(cachedChartList)
        }

        println("[CACHE] ↓ Fetching chart list from network (chart=$chartId, date=$effectiveDate)...")
        return network.getChartByDate(chartId, date).onSuccess { chartList ->
            val listToCache =
                chartList.copy(
                    week = chartList.week?.copy(date = effectiveDate),
                )
            database.insertChartList(listToCache)
            val trackCount = chartList.chartTracks?.size ?: 0
            println("[CACHE] ✓ Cached chart list (chart=$chartId, date=$effectiveDate, tracks=$trackCount)")

            // Fetch artwork for tracks in the background
            fetchArtworkForTracks(chartList)
        }.recoverCatching { networkError ->
            val fallbackChartList = database.getChartListByDate(chartId, effectiveDate)
            if (fallbackChartList != null) {
                println("[CACHE] ⚠ Network failed, using stale cache (chart=$chartId, date=$effectiveDate)")
                fallbackChartList
            } else {
                println("[CACHE] ✗ Network failed, no cache available (chart=$chartId, date=$effectiveDate)")
                throw networkError
            }
        }
    }

    /**
     * Fetch artwork URLs for all tracks in a chart list.
     * Launches in background scope - does NOT block chart display.
     * UI updates reactively as artwork URLs are fetched.
     */
    private fun fetchArtworkForTracks(chartList: ChartList) {
        artworkScope.launch {
            val tracks = chartList.chartTracks?.mapNotNull { it.track } ?: return@launch

            println("[ARTWORK] Starting background fetch for ${tracks.size} tracks...")

            // Keep track of updated chart list
            var updatedChartList = chartList

            // Fetch artwork for each track individually
            tracks.forEach { track ->
                val trackId = track.id ?: return@forEach

                // Skip if already has artwork
                if (!track.artworkUrl.isNullOrBlank()) {
                    return@forEach
                }

                // Fetch artwork URL
                val artworkUrl = artworkRepository.getArtworkUrl(track)

                if (artworkUrl != null) {
                    // Update this track in the chart list
                    val updatedTrack = track.copy(artworkUrl = artworkUrl)
                    updatedChartList =
                        updatedChartList.copy(
                            chartTracks =
                                updatedChartList.chartTracks?.map { chartTrack ->
                                    if (chartTrack.track?.id == trackId) {
                                        chartTrack.copy(track = updatedTrack)
                                    } else {
                                        chartTrack
                                    }
                                },
                        )

                    // Update database (no-op on wasmJs)
                    database.insertChartList(updatedChartList)

                    // Emit update to UI
                    _chartUpdates.emit(updatedChartList)

                    println("[ARTWORK] ✓ Updated track: ${track.artistName} - ${track.title}")
                    println("[ARTWORK] → Emitted update via SharedFlow")
                }
            }

            println("[ARTWORK] ✓ Background artwork fetch complete")
        }
    }
}
