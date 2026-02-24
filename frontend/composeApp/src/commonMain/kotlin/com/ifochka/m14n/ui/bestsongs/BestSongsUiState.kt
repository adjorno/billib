package com.ifochka.m14n.ui.bestsongs

import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.Track

data class BestSongsFilter(
    val selectedChart: Chart,
    val dateRange: DateRange = DateRange.AllTime,
)

sealed interface BestSongsUiState {
    data object Loading : BestSongsUiState

    data class Success(
        val availableCharts: List<Chart>,
        val filter: BestSongsFilter,
        val tracks: List<Track>,
    ) : BestSongsUiState

    data class Error(
        val message: String,
    ) : BestSongsUiState
}
