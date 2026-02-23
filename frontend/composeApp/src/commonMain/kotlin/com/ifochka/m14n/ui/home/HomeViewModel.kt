package com.ifochka.m14n.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.api.M14nApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val api: M14nApi,
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
}
