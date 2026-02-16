package com.ifochka.billib.rest

interface ITrendsController {
    fun generateTrends(
        week: String,
        type: Long,
    )
}
