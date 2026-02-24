package com.ifochka.m14n.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult<T>(
    val offset: Int = 0,
    val results: List<T>? = null,
    val total: Int = 0,
)

@Serializable
data class MergedSearchResult(
    val artists: SearchResult<Artist>? = null,
    val tracks: SearchResult<Track>? = null,
)
