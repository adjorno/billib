package com.m14n.billib.rest

import com.m14n.billib.data.chart.Chart
import com.m14n.billib.data.chartlist.ChartList
import retrofit2.http.GET
import retrofit2.http.Query

interface BilliBService {
    @GET("chart/all")
    suspend fun listCharts(
    ): List<Chart>

    @GET("/chartList/getByDate")
    suspend fun chartListByChartAndDate(
        @Query("chart_id")
        chartId: Long,
        @Query("date")
        date: String
    ): ChartList
}
