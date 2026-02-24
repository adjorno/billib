package com.ifochka.m14n.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.ArtworkRepository
import com.ifochka.m14n.data.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val api: M14nApi,
    private val artworkRepository: ArtworkRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            val dayTrackDeferred = async { api.getDayTrack() }
            val trendsDeferred = async { api.getTrends() }

            val dayTrackResult = dayTrackDeferred.await()
            val trendsResult = trendsDeferred.await()

            val error = dayTrackResult.exceptionOrNull() ?: trendsResult.exceptionOrNull()
            if (error != null) {
                _uiState.value = HomeUiState.Error(error.message ?: "Failed to load")
                return@launch
            }

            _uiState.value = HomeUiState.Success(
                dayTrack = dayTrackResult.getOrThrow(),
                trends = trendsResult.getOrThrow(),
            )
        }
    }

    suspend fun loadArtworkForDayTrack() {
        val track = (_uiState.value as? HomeUiState.Success)?.dayTrack?.track ?: return
        val url = artworkRepository.getArtworkUrl(track) ?: return
        _uiState.update { state ->
            (state as? HomeUiState.Success)?.let { s ->
                s.copy(dayTrack = s.dayTrack.copy(track = s.dayTrack.track?.copy(artworkUrl = url)))
            } ?: state
        }
    }

    suspend fun loadArtworkForTrendTrack(track: Track) {
        val trackId = track.id ?: return
        val url = artworkRepository.getArtworkUrl(track) ?: return
        _uiState.update { state ->
            (state as? HomeUiState.Success)?.let { s ->
                s.copy(
                    trends = s.trends.copy(
                        trendLists = s.trends.trendLists?.map { list ->
                            list.copy(
                                tracks = list.tracks?.map { t ->
                                    if (t.id == trackId) t.copy(artworkUrl = url) else t
                                },
                            )
                        },
                    ),
                )
            } ?: state
        }
    }
}
