package com.ifochka.m14n.ui.bestsongs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BestSongsViewModel(
    private val api: M14nApi,
    private val artworkRepository: ArtworkRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<BestSongsUiState>(BestSongsUiState.Loading)
    val uiState: StateFlow<BestSongsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = BestSongsUiState.Loading
            api.getAllCharts()
                .onSuccess { charts ->
                    if (charts.isEmpty()) {
                        _uiState.value = BestSongsUiState.Error("No charts available")
                        return@onSuccess
                    }
                    val filter = BestSongsFilter(selectedChart = charts.first())
                    loadTracks(
                        charts = charts,
                        filter = filter,
                    )
                }
                .onFailure {
                    _uiState.value = BestSongsUiState.Error(it.message ?: "Failed to load")
                }
        }
    }

    fun applyFilter(filter: BestSongsFilter) {
        val charts = (_uiState.value as? BestSongsUiState.Success)?.availableCharts ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadTracks(
                charts = charts,
                filter = filter,
            )
        }
    }

    private suspend fun loadTracks(
        charts: List<Chart>,
        filter: BestSongsFilter,
    ) {
        val chartId = filter.selectedChart.id ?: return
        val (from, to) = filter.dateRange.toFromTo()
        val current = _uiState.value as? BestSongsUiState.Success
        if (current != null) {
            _uiState.value = current.copy(filter = filter, isLoadingTracks = true)
        } else {
            _uiState.value = BestSongsUiState.Loading
        }
        api.getBestTracks(
            chartId = chartId,
            from = from,
            to = to,
        )
            .onSuccess { tracks ->
                _uiState.value = BestSongsUiState.Success(
                    availableCharts = charts,
                    filter = filter,
                    tracks = tracks,
                    isLoadingTracks = false,
                )
            }
            .onFailure {
                _uiState.value = BestSongsUiState.Error(it.message ?: "Failed to load tracks")
            }
    }

    suspend fun loadArtworkForTrack(track: Track) {
        val trackId = track.id ?: return
        val url = artworkRepository.getArtworkUrl(track) ?: return
        _uiState.update { state ->
            (state as? BestSongsUiState.Success)?.let { s ->
                s.copy(
                    tracks = s.tracks.map { t ->
                        if (t.id == trackId) t.copy(artworkUrl = url) else t
                    },
                )
            } ?: state
        }
    }
}
