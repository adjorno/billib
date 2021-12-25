package com.adjorno.billib.rest.model

data class Trends(
    val week: String,
    val trendLists: Array<TrendList>
)
