package com.ifochka.m14n.ui.artistdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.model.Artist
import com.ifochka.m14n.data.model.Track
import com.ifochka.m14n.ui.artistdetails.ArtistDetailsUiState.Loading
import com.ifochka.m14n.ui.artistdetails.ArtistDetailsUiState.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtistDetailsViewModel(
    private val api: M14nApi,
    private val artworkRepository: ArtworkRepository,
    private val artistId: Long,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ArtistDetailsUiState>(Loading)
    val uiState: StateFlow<ArtistDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = api.getArtistInfo(artistId)
                .fold(
                    onSuccess = { info ->
                        val heroUrl = info.tracks?.firstOrNull()
                            ?.let { artworkRepository.getArtworkUrl(it) }
                        Success(
                            artist = info.artist,
                            globalRank = info.globalRank,
                            tracks = info.tracks ?: emptyList(),
                            relations = info.artistRelations ?: emptyList(),
                            artworkUrl = heroUrl,
                        )
                    },
                    onFailure = { ArtistDetailsUiState.Error(it.message ?: "Unknown error") },
                )
        }
    }

    suspend fun loadArtworkForRelation(artist: Artist) {
        val url = artworkRepository.getArtworkUrlForArtist(artist) ?: return
        _uiState.update { state ->
            if (state !is Success) return@update state
            state.copy(
                relations = state.relations.map { a ->
                    if (a.id == artist.id) a.copy(artworkUrl = url) else a
                },
            )
        }
    }

    suspend fun loadArtworkForTrack(track: Track) {
        val url = artworkRepository.getArtworkUrl(track) ?: return
        _uiState.update { state ->
            if (state !is Success) return@update state
            state.copy(
                tracks = state.tracks.map { t ->
                    if (t.id == track.id) t.copy(artworkUrl = url) else t
                },
            )
        }
    }
}
