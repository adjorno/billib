package com.ifochka.m14n.rest.model

data class Trends(
    val week: String,
    val trendLists: Array<TrendList>,
)
