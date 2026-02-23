package com.ifochka.m14n.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Trends(
    val week: String? = null,
    val trendLists: List<TrendList>? = null,
)
