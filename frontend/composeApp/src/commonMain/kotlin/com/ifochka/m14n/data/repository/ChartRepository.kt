package com.ifochka.m14n.data.repository

import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList
import com.ifochka.m14n.data.model.Track

class ChartRepository(
    private val cachedRepository: CachedChartRepository,
) {
    suspend fun getAllCharts(): Result<List<Chart>> = cachedRepository.getAllCharts()

    suspend fun getChartByDate(
        chartId: Long,
        date: String? = null,
    ): Result<ChartList> = cachedRepository.getChartByDate(chartId, date)

    suspend fun getArtworkUrl(track: Track): String? = cachedRepository.getArtworkUrl(track)
}
