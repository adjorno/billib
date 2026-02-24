package com.ifochka.m14n.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.model.Track
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val QUERY_DEBOUNCE_MS = 300L

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val api: M14nApi,
    private val artworkRepository: ArtworkRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init {
        viewModelScope.launch {
            _query.debounce(QUERY_DEBOUNCE_MS).collect { q ->
                if (q.isBlank()) {
                    _uiState.value = SearchUiState.Idle
                } else {
                    performSearch(q)
                }
            }
        }
    }

    fun setQuery(q: String) {
        _query.value = q
    }

    fun retry() {
        val q = _query.value
        if (q.isNotBlank()) {
            viewModelScope.launch { performSearch(q) }
        }
    }

    private suspend fun performSearch(q: String) {
        _uiState.value = SearchUiState.Loading
        api.search(query = q).fold(
            onSuccess = { result ->
                _uiState.value = SearchUiState.Results(
                    artists = result.artists?.results ?: emptyList(),
                    artistsTotal = result.artists?.total ?: 0,
                    tracks = result.tracks?.results ?: emptyList(),
                    tracksTotal = result.tracks?.total ?: 0,
                )
            },
            onFailure = { error ->
                _uiState.value = SearchUiState.Error(
                    message = error.message ?: "Search failed",
                )
            },
        )
    }

    suspend fun loadArtworkForTrack(track: Track) {
        val trackId = track.id ?: return
        val url = artworkRepository.getArtworkUrl(track) ?: return
        _uiState.update { state ->
            (state as? SearchUiState.Results)?.let { s ->
                s.copy(
                    tracks = s.tracks.map { t ->
                        if (t.id == trackId) t.copy(artworkUrl = url) else t
                    },
                )
            } ?: state
        }
    }
}
