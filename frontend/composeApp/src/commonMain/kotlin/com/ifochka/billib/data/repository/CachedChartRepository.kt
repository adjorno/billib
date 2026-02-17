package com.ifochka.billib.data.repository

import com.ifochka.billib.data.artwork.ArtworkRepository
import com.ifochka.billib.data.db.CachePolicy
import com.ifochka.billib.data.db.ChartDatabaseRepository
import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CachedChartRepository(
    private val database: ChartDatabaseRepository,
    private val network: NetworkChartRepository,
    private val artworkRepository: ArtworkRepository,
) {
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
     * Runs asynchronously to avoid blocking chart display.
     */
    private suspend fun fetchArtworkForTracks(chartList: ChartList) =
        coroutineScope {
            async {
                val tracks = chartList.chartTracks?.mapNotNull { it.track } ?: return@async

                println("[ARTWORK] Fetching artwork for ${tracks.size} tracks...")
                val artworkUrls = artworkRepository.getArtworkUrls(tracks)

                // Update tracks with artwork URLs
                artworkUrls.forEach { (trackId, artworkUrl) ->
                    if (artworkUrl != null) {
                        val track = tracks.find { it.id == trackId }
                        if (track != null) {
                            val updatedTrack = track.copy(artworkUrl = artworkUrl)
                            // Re-insert track to update artwork_url in database
                            database.insertChartList(
                                chartList.copy(
                                    chartTracks =
                                        chartList.chartTracks.map { chartTrack ->
                                            if (chartTrack.track?.id == trackId) {
                                                chartTrack.copy(track = updatedTrack)
                                            } else {
                                                chartTrack
                                            }
                                        },
                                ),
                            )
                        }
                    }
                }

                val fetchedCount = artworkUrls.values.count { it != null }
                println("[ARTWORK] ✓ Fetched $fetchedCount/${tracks.size} artwork URLs")
            }
        }
}
