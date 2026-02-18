package com.ifochka.billib.data.repository

import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList
import com.ifochka.billib.data.model.Track

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
