package com.ifochka.m14n.rest.trends.rest.dtos

data class Trends(
    val week: String,
    val trendLists: Array<TrendList>,
)
