package com.ifochka.billib.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ifochka.billib.ui.chart.components.ChartTopBar
import com.ifochka.billib.ui.chart.components.ChartTrackList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChartScreen(viewModel: ChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        when (val state = uiState) {
            is ChartUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ChartUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    ChartTopBar(
                        weekDate = state.selectedWeek,
                        availableCharts = state.availableCharts,
                        selectedChart = state.selectedChart,
                        onChartSelected = { chartId ->
                            viewModel.selectChart(chartId)
                        },
                        onWeekNavigate = { direction ->
                            viewModel.navigateWeek(direction)
                        },
                        onWeekSelect = { weekDate ->
                            viewModel.selectWeek(
                                chartId = state.selectedChart.id ?: return@ChartTopBar,
                                weekDate = weekDate,
                            )
                        },
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    state.chartList.chartTracks?.let { tracks ->
                        ChartTrackList(
                            chartTracks = tracks,
                            onArtworkNeeded = viewModel::loadArtworkForTrack,
                        )
                    }
                }
            }

            is ChartUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.error.toDisplayMessage(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
