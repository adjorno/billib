package com.ifochka.m14n.data.repository

import com.ifochka.m14n.data.api.BillibApi
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList

class NetworkChartRepository(
    private val api: BillibApi,
) {
    suspend fun getAllCharts(): Result<List<Chart>> = api.getAllCharts()

    suspend fun getChartByDate(
        chartId: Long,
        date: String?,
    ): Result<ChartList> = api.getChartByDate(chartId, date)
}
