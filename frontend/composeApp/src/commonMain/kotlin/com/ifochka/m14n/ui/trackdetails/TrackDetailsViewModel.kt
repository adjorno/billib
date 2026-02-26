package com.ifochka.m14n.ui.trackdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                    val history = info.history
                    val allDates = history?.values?.flatMap { it.keys } ?: emptyList()
                    val artworkUrl = artworkRepository.getArtworkUrl(info.track)
                    _uiState.value = TrackDetailsUiState.Success(
                        track = info.track.copy(
                            artworkUrl = artworkUrl ?: info.track.artworkUrl,
                            firstChartDate = info.track.firstChartDate ?: allDates.minOrNull(),
                            totalWeeksOnChart = info.track.totalWeeksOnChart.takeIf { it > 0 }
                                ?: history?.values?.sumOf { it.size } ?: 0,
                        ),
                        history = history,
                        globalRank = info.globalRank,
                    )
                }
                .onFailure { error ->
                    _uiState.value = TrackDetailsUiState.Error(error.message ?: "Failed to load")
                }
        }
    }
}
