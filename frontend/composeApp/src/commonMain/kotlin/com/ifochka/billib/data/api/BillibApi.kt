package com.ifochka.billib.data.api

import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList

interface BillibApi {
    suspend fun getAllCharts(): Result<List<Chart>>

    suspend fun getChartByDate(
        chartId: Long,
        date: String? = null,
    ): Result<ChartList>
}
