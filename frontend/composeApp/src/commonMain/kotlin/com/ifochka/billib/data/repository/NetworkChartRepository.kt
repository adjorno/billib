package com.ifochka.billib.data.repository

import com.ifochka.billib.data.api.BillibApi
import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList

class NetworkChartRepository(
    private val api: BillibApi,
) {
    suspend fun getAllCharts(): Result<List<Chart>> = api.getAllCharts()

    suspend fun getChartByDate(
        chartId: Long,
        date: String?,
    ): Result<ChartList> = api.getChartByDate(chartId, date)
}
