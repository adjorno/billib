package com.ifochka.m14n.importer.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BBJournalMetadata(
    val name: String,
    val url: String,
    val charts: List<BBChartMetadata>,
)

@Serializable
data class BBChartMetadata(
    val name: String,
    val folder: String,
    val size: Int,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String? = null,
    val prefix: String,
)

@Serializable
data class BBChart(
    val name: String,
    @SerialName("chart_date")
    val date: String,
    val tracks: List<BBTrack>,
)

@Serializable
data class BBTrack(
    val rank: Int,
    val title: String,
    val artist: String,
    val position: BBPositionInfo? = null,
)

@Serializable
data class BBPositionInfo(
    @SerialName("Last Week")
    val lastWeek: String? = null,
    @SerialName("Peak Position")
    val peakPosition: Int = 0,
    @SerialName("Wks on Chart")
    val wksOnChart: Int = 0,
)
