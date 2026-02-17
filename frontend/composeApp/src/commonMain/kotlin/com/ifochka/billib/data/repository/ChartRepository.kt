package com.ifochka.billib.data.repository

import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList
import kotlinx.coroutines.flow.SharedFlow

class ChartRepository(
    private val cachedRepository: CachedChartRepository,
) {
    suspend fun getAllCharts(): Result<List<Chart>> = cachedRepository.getAllCharts()

    suspend fun getChartByDate(
        chartId: Long,
        date: String? = null,
    ): Result<ChartList> = cachedRepository.getChartByDate(chartId, date)

    /**
     * Flow that emits whenever a track's artwork is updated.
     * Subscribe to this to reactively update the UI as artwork loads.
     */
    val chartUpdates: SharedFlow<ChartList> = cachedRepository.chartUpdates
}
