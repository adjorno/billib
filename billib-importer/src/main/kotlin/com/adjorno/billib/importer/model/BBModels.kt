package com.adjorno.billib.importer.model

import com.google.gson.annotations.SerializedName

data class BBJournalMetadata(
    val name: String,
    val url: String,
    val charts: List<BBChartMetadata>
)

data class BBChartMetadata(
    val name: String,
    val folder: String,
    val size: Int,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String? = null,
    val prefix: String
)

data class BBChart(
    val name: String,
    @SerializedName("chart_date")
    val date: String,
    val tracks: List<BBTrack>
)

data class BBTrack(
    val rank: Int,
    val title: String,
    val artist: String,
    val position: BBPositionInfo? = null
)

data class BBPositionInfo(
    @SerializedName("Last Week")
    val lastWeek: String? = null,
    @SerializedName("Peak Position")
    val peakPosition: Int = 0,
    @SerializedName("Wks on Chart")
    val wksOnChart: Int = 0
)
