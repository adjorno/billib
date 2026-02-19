package com.ifochka.m14n.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifochka.m14n.data.error.ErrorMapper
import com.ifochka.m14n.data.model.Track
import com.ifochka.m14n.data.repository.ChartRepository
import com.ifochka.m14n.data.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChartViewModel(
    private val repository: ChartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Loading)
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    init {
        loadCharts()
    }

    suspend fun loadArtworkForTrack(track: Track) {
        val trackId = track.id ?: return
        repository.getArtworkUrl(track)?.let { artworkUrl ->
            _uiState.update { currentState ->
                (currentState as? ChartUiState.Success)?.let { successState ->
                    val updatedTracks = successState.chartList.chartTracks?.map { chartTrack ->
                        if (chartTrack.track?.id == trackId) {
                            chartTrack.copy(track = chartTrack.track.copy(artworkUrl = artworkUrl))
                        } else {
                            chartTrack
                        }
                    }
                    successState.copy(
                        chartList = successState.chartList.copy(chartTracks = updatedTracks),
                    )
                } ?: currentState
            }
        }
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
        availableCharts: List<com.ifochka.m14n.data.model.Chart>,
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

    /**
     * Navigate to a specific week for the current chart.
     * @param chartId The chart to navigate
     * @param weekDate Monday date in "yyyy-MM-dd" format
     */
    fun selectWeek(
        chartId: Long,
        weekDate: String,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ChartUiState.Success) return@launch

            // Validate date format
            val date = DateUtils.parseChartDate(weekDate) ?: return@launch

            // Validate date is within chart range
            val mondayDate = DateUtils.getMondayOfWeek(date)
            if (!DateUtils.isDateInRange(
                    mondayDate,
                    currentState.selectedChart.startDate,
                    currentState.selectedChart.endDate,
                )
            ) {
                return@launch
            }

            // Navigate to the week
            selectChart(chartId, DateUtils.formatChartDate(mondayDate))
        }
    }

    /**
     * Navigate to previous or next week from current selection.
     */
    fun navigateWeek(direction: WeekDirection) {
        val currentState = _uiState.value
        if (currentState !is ChartUiState.Success) return

        val currentDate = DateUtils.parseChartDate(currentState.selectedWeek)
        val chartId = currentState.selectedChart.id
        if (currentDate == null || chartId == null) return

        val newDate = when (direction) {
            WeekDirection.PREVIOUS -> DateUtils.getPreviousWeek(currentDate)
            WeekDirection.NEXT -> DateUtils.getNextWeek(currentDate)
        }

        if (DateUtils.isDateInRange(
                newDate,
                currentState.selectedChart.startDate,
                currentState.selectedChart.endDate,
            )
        ) {
            selectWeek(chartId, DateUtils.formatChartDate(newDate))
        }
    }
}

enum class WeekDirection {
    PREVIOUS,
    NEXT,
}
