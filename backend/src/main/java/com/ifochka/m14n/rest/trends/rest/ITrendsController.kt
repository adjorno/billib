package com.ifochka.m14n.rest.trends.rest

interface ITrendsController {
    fun generateTrends(
        week: String,
        type: Long,
    )
}
