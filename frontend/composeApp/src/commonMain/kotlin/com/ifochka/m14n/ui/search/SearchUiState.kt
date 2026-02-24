package com.ifochka.m14n.ui.search

import com.ifochka.m14n.data.model.Artist
import com.ifochka.m14n.data.model.Track

sealed interface SearchUiState {
    data object Idle : SearchUiState

    data object Loading : SearchUiState

    data class Results(
        val artists: List<Artist>,
        val artistsTotal: Int,
        val tracks: List<Track>,
        val tracksTotal: Int,
    ) : SearchUiState

    data class Error(
        val message: String,
    ) : SearchUiState
}
