package com.ifochka.m14n.rest.catalog.domain

interface TrackHistoryPort {
    fun getTrackHistory(
        trackId: Long,
        chartId: Long?,
    ): Map<String, Map<String, Int>>
}
