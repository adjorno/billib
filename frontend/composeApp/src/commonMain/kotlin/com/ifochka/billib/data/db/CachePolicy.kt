package com.ifochka.billib.data.db

import kotlin.time.Duration.Companion.days

expect fun currentTimeMillis(): Long

object CachePolicy {
    const val MAX_CHARTS_COUNT = 50
    val CACHE_TTL_MS = 7.days.inWholeMilliseconds

    fun isCacheStale(
        cachedAt: Long,
        ttlMs: Long,
    ): Boolean {
        val now = currentTimeMillis()
        return (now - cachedAt) > ttlMs
    }
}
