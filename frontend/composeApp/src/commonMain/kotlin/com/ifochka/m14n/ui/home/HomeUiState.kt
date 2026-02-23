package com.ifochka.m14n.ui.home

import com.ifochka.m14n.data.model.DayTrack
import com.ifochka.m14n.data.model.Trends

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val dayTrack: DayTrack,
        val trends: Trends,
    ) : HomeUiState

    data class Error(
        val message: String,
    ) : HomeUiState
}
