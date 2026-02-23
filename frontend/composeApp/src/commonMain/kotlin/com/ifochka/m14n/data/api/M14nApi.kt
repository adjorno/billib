package com.ifochka.m14n.data.api

import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList
import com.ifochka.m14n.data.model.DayTrack
import com.ifochka.m14n.data.model.Trends

interface M14nApi {
    suspend fun getAllCharts(): Result<List<Chart>>

    suspend fun getChartByDate(
        chartId: Long,
        date: String? = null,
    ): Result<ChartList>

    suspend fun getTrends(date: String? = null): Result<Trends>

    suspend fun getDayTrack(date: String? = null): Result<DayTrack>
}
