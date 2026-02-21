package com.ifochka.m14n.data.repository

import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.db.CachePolicy
import com.ifochka.m14n.data.db.ChartDatabaseRepository
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList
import com.ifochka.m14n.data.model.Track

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
        val cachedTimestamp = database.getChartListCachedAt(chartId, date)
        val cachedChartList = cachedTimestamp?.let {
            val isStale = CachePolicy.isCacheStale(it, CachePolicy.CACHE_TTL_MS)
            if (!isStale) {
                database.getChartListByDate(chartId, date)
            } else {
                println("[CACHE] ⏱ Chart list cache is stale (chart=$chartId, date=$date)")
                null
            }
        }

        if (cachedChartList != null) {
            println(
                "[CACHE] ✓ Chart list loaded from cache (chart=$chartId, date=${cachedChartList.week?.date}, ttl=7d)",
            )
            return Result.success(cachedChartList)
        }

        println("[CACHE] ↓ Fetching chart list from network (chart=$chartId, date=$date)...")
        return network.getChartByDate(chartId, date).mapCatching { networkChartList ->
            database.insertChartList(networkChartList)
            val trackCount = networkChartList.chartTracks?.size ?: 0
            println(
                "[CACHE] ✓ Cached chart list (chart=$chartId, date=${networkChartList.week?.date}, tracks=$trackCount)",
            )
            networkChartList
        }.recoverCatching { networkError ->
            val fallbackChartList = database.getChartListByDate(chartId, date)
            if (fallbackChartList != null) {
                println(
                    "[CACHE] ⚠ Network failed, using stale cache (chart=$chartId, date=${fallbackChartList.week?.date})",
                )
                fallbackChartList
            } else {
                println("[CACHE] ✗ Network failed, no cache available (chart=$chartId, date=$date)")
                throw networkError
            }
        }
    }

    /**
     * Fetch artwork URL for a single track.
     * Persistence is handled by ArtworkRepository via ArtworkUrlPersistence.
     */
    suspend fun getArtworkUrl(track: Track): String? = artworkRepository.getArtworkUrl(track)
}
