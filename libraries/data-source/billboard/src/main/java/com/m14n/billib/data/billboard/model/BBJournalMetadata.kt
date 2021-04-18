package com.m14n.billib.data.billboard.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BBJournalMetadata(
    var name: String,

    var url: String,

    @SerialName("base_rss")
    var baseRss: String,

    var charts: List<BBChartMetadata>
)