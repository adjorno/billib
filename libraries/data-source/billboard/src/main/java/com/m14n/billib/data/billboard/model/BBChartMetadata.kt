package com.m14n.billib.data.billboard.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BBChartMetadata(
    var name: String,
    var folder: String,
    var size: Int,
    @SerialName("start_date")
    var startDate: String,
    @SerialName("end_date")
    var endDate: String? = null,
    var prefix: String
)
