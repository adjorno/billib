package com.ifochka.m14n.data.api

import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList

interface M14nApi {
    suspend fun getAllCharts(): Result<List<Chart>>

    suspend fun getChartByDate(
        chartId: Long,
        date: String? = null,
    ): Result<ChartList>
}
