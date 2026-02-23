package com.ifochka.m14n.data.api

import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList
import com.ifochka.m14n.data.model.DayTrack
import com.ifochka.m14n.data.model.Trends
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorM14nApi(
    private val httpClient: HttpClient,
) : M14nApi {
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

    override suspend fun getTrends(date: String?): Result<Trends> =
        runCatching {
            httpClient.get("/trends") {
                date?.let { parameter("date", it) }
            }.body()
        }

    override suspend fun getDayTrack(date: String?): Result<DayTrack> =
        runCatching {
            httpClient.get("/track/day") {
                date?.let { parameter("date", it) }
            }.body()
        }
}
