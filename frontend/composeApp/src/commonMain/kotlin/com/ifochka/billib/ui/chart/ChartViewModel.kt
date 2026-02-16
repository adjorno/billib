package com.ifochka.billib.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.billib.data.error.ErrorMapper
import com.ifochka.billib.data.repository.ChartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChartViewModel(
    private val repository: ChartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Loading)
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    init {
        loadCharts()
    }

    fun loadCharts() {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading

            repository.getAllCharts()
                .onSuccess { charts ->
                    if (charts.isNotEmpty()) {
                        // Load the first chart by default (usually Hot 100)
                        selectChart(charts.first().id ?: return@launch)
                    } else {
                        _uiState.value = ChartUiState.Error(ErrorMapper.mapEmptyCharts())
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = ChartUiState.Error(ErrorMapper.mapError(throwable))
                }
        }
    }

    fun selectChart(
        chartId: Long,
        date: String? = null,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ChartUiState.Success) {
                // If not in success state, we need to load all charts first
                val chartsResult = repository.getAllCharts()
                chartsResult.onFailure { throwable ->
                    _uiState.value = ChartUiState.Error(ErrorMapper.mapError(throwable))
                    return@launch
                }

                val charts = chartsResult.getOrNull() ?: return@launch
                loadChartData(charts, chartId, date)
            } else {
                // We already have charts, just load the new chart data
                loadChartData(currentState.availableCharts, chartId, date)
            }
        }
    }

    private suspend fun loadChartData(
        availableCharts: List<com.ifochka.billib.data.model.Chart>,
        chartId: Long,
        date: String? = null,
    ) {
        repository.getChartByDate(chartId, date)
            .onSuccess { chartList ->
                val selectedChart = availableCharts.find { it.id == chartId }
                    ?: availableCharts.first()

                _uiState.value = ChartUiState.Success(
                    availableCharts = availableCharts,
                    selectedChart = selectedChart,
                    chartList = chartList,
                    selectedWeek = chartList.week?.date ?: "Unknown",
                )
            }
            .onFailure { throwable ->
                _uiState.value = ChartUiState.Error(ErrorMapper.mapError(throwable))
            }
    }

    fun retry() {
        loadCharts()
    }
}
