package com.ifochka.m14n.ui.trackdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TrackDetailsUiState {
    data object Loading : TrackDetailsUiState

    data class Success(
        val track: Track,
        val history: Map<String, Map<String, Int>>? = null,
        val globalRank: Long = 0,
    ) : TrackDetailsUiState

    data class Error(
        val message: String,
    ) : TrackDetailsUiState
}

class TrackDetailsViewModel(
    private val api: M14nApi,
    private val artworkRepository: ArtworkRepository,
    private val trackId: Long,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TrackDetailsUiState>(TrackDetailsUiState.Loading)
    val uiState: StateFlow<TrackDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            api.getTrackInfo(trackId)
                .onSuccess { info ->
                    _uiState.value = TrackDetailsUiState.Success(
                        track = info.track,
                        history = info.history,
                        globalRank = info.globalRank,
                    )
                }
                .onFailure { error ->
                    _uiState.value = TrackDetailsUiState.Error(error.message ?: "Failed to load")
                }
        }
    }

    suspend fun loadArtwork(track: Track) {
        val url = artworkRepository.getArtworkUrl(track) ?: return
        _uiState.update { state ->
            (state as? TrackDetailsUiState.Success)
                ?.copy(track = state.track.copy(artworkUrl = url)) ?: state
        }
    }
}
