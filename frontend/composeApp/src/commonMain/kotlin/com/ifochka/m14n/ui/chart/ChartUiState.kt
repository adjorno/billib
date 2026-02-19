package com.ifochka.m14n.ui.chart

import com.ifochka.m14n.data.error.ChartError
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartList

sealed interface ChartUiState {
    data object Loading : ChartUiState

    data class Success(
        val availableCharts: List<Chart>,
        val selectedChart: Chart,
        val chartList: ChartList,
        val selectedWeek: String,
    ) : ChartUiState

    data class Error(
        val error: ChartError,
    ) : ChartUiState
}
