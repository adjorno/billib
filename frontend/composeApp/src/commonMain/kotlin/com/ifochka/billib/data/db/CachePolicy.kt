package com.ifochka.billib.data.db

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

expect fun currentTimeMillis(): Long

object CachePolicy {
    val CHARTS_TTL_MS = 24.hours.inWholeMilliseconds
    val CHART_LIST_TTL_MS = 7.days.inWholeMilliseconds
    val LATEST_CHART_TTL_MS = 5.minutes.inWholeMilliseconds

    fun isCacheStale(
        cachedAt: Long,
        ttlMs: Long,
    ): Boolean {
        val now = currentTimeMillis()
        return (now - cachedAt) > ttlMs
    }
}
