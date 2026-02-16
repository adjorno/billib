package com.ifochka.billib.ui.chart

import com.ifochka.billib.data.error.ChartError
import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList

sealed interface ChartUiState {
    data object Loading : ChartUiState

    data class Success(
        val availableCharts: List<Chart>,
        val selectedChart: Chart,
        val chartList: ChartList,
        val selectedWeek: String,
    ) : ChartUiState

    data class Error(
        val error: com.ifochka.billib.data.error.ChartError,
    ) : ChartUiState
}
