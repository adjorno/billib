package com.ifochka.m14n.data.api

import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorBillibApi(
    private val httpClient: HttpClient,
) : BillibApi {
    override suspend fun getAllCharts(): Result<List<Chart>> =
        runCatching {
            httpClient.get("/chart/all").body()
        }

    override suspend fun getChartByDate(
        chartId: Long,
        date: String?,
    ): Result<ChartList> =
        runCatching {
            httpClient.get("/chartList/getByDate") {
                parameter("chart_id", chartId)
                date?.let { parameter("date", it) }
            }.body()
        }
}
