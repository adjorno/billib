package com.ifochka.billib.data.repository

import com.ifochka.billib.data.db.CachePolicy
import com.ifochka.billib.data.db.ChartDatabaseRepository
import com.ifochka.billib.data.db.currentTimeMillis
import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList

class CachedChartRepository(
    private val database: ChartDatabaseRepository,
    private val network: NetworkChartRepository,
) {
    suspend fun getAllCharts(): Result<List<Chart>> {
        val cachedCharts = database.getAllCharts()

        if (cachedCharts.isNotEmpty()) {
            val count = cachedCharts.size
            if (count <= CachePolicy.MAX_CHARTS_COUNT) {
                return Result.success(cachedCharts)
            }
        }

        return network.getAllCharts().onSuccess { charts ->
            val limitedCharts = charts.take(CachePolicy.MAX_CHARTS_COUNT)
            database.insertCharts(limitedCharts)
        }.recoverCatching { networkError ->
            if (cachedCharts.isNotEmpty()) {
                cachedCharts
            } else {
                throw networkError
            }
        }
    }

    suspend fun getChartByDate(
        chartId: Long,
        date: String?,
    ): Result<ChartList> {
        val isLatestChart = date == null
        val effectiveDate = date ?: "latest"

        val cachedTimestamp = database.getChartListCachedAt(chartId, effectiveDate)
        val cachedChartList = cachedTimestamp?.let {
            val ttl =
                if (isLatestChart) {
                    CachePolicy.LATEST_CHART_TTL_MS
                } else {
                    CachePolicy.CHART_LIST_TTL_MS
                }
            if (!CachePolicy.isCacheStale(it, ttl)) {
                database.getChartListByDate(chartId, effectiveDate)
            } else {
                null
            }
        }

        if (cachedChartList != null) {
            return Result.success(cachedChartList)
        }

        return network.getChartByDate(chartId, date).onSuccess { chartList ->
            val listToCache =
                chartList.copy(
                    week = chartList.week?.copy(date = effectiveDate),
                )
            database.insertChartList(listToCache)
        }.recoverCatching { networkError ->
            val fallbackChartList = database.getChartListByDate(chartId, effectiveDate)
            if (fallbackChartList != null) {
                fallbackChartList
            } else {
                throw networkError
            }
        }
    }
}
