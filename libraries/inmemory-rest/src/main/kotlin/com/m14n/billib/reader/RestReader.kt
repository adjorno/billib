package com.m14n.billib.reader

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.m14n.billib.data.billboard.date
import com.m14n.billib.data.billboard.generateBillboardDateSequence
import com.m14n.billib.data.billboard.text
import com.m14n.billib.rest.BilliBService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
            load(stream)
        }
    }
    val todayDate = properties.getProperty("data.today").date
    val retrofit = Retrofit.Builder()
        .baseUrl("http://localhost:8080/")
        .addConverterFactory(
            Json {
                ignoreUnknownKeys = true
            }.asConverterFactory("application/json".toMediaType())
        )
        .build()

    val service = retrofit.create(BilliBService::class.java)
    val charts = runBlocking {
        service.listCharts().onEach { chart ->
            println("Chart ${chart.name}")
            chart.chartLists = generateBillboardDateSequence(
                chart.startDate,
                chart.endDate ?: todayDate
            ).toList().map { date -> date.text }.map { week ->
                println("Week $week")
                service.chartListByChartAndDate(chart.id, week).also { chartList ->
                    chartList.chartTracks?.forEach { chartTrack ->
                        chartTrack.chartList = chartList
                    }
                }
            }
        }
    }
}
