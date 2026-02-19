package com.ifochka.m14n.rest

interface ITrendsController {
    fun generateTrends(
        week: String,
        type: Long,
    )
}
