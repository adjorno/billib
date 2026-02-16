package com.ifochka.billib.rest.model

data class Trends(
    val week: String,
    val trendLists: Array<TrendList>
)
