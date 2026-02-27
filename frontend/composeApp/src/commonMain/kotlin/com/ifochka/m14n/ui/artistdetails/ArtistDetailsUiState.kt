package com.ifochka.m14n.ui.artistdetails

import com.ifochka.m14n.data.model.Artist
import com.ifochka.m14n.data.model.Track

sealed interface ArtistDetailsUiState {
    data object Loading : ArtistDetailsUiState

    data class Success(
        val artist: Artist,
        val globalRank: Long,
        val tracks: List<Track>,
        val relations: List<Artist>,
        val artworkUrl: String? = null,
    ) : ArtistDetailsUiState

    data class Error(
        val message: String,
    ) : ArtistDetailsUiState
}
