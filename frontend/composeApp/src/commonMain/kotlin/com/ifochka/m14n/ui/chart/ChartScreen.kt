package com.ifochka.m14n.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.ChartTrack
import com.ifochka.m14n.share.ShareManager
import com.ifochka.m14n.ui.chart.components.ChartTopBar
import com.ifochka.m14n.ui.chart.components.ChartTrackList
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChartScreen(viewModel: ChartViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val shareManager: ShareManager = koinInject()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun buildShareText(chartTrack: ChartTrack): String? {
        val track = chartTrack.track ?: return null
        val artist = track.artist?.name ?: track.artistName ?: "Unknown Artist"
        val title = track.title ?: "Unknown Title"
        return "$artist \u2014 $title"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

                    key(state.selectedChart.id, state.selectedWeek) {
                        state.chartList.chartTracks?.let { tracks ->
                            ChartTrackList(
                                chartTracks = tracks,
                                onArtworkNeeded = viewModel::loadArtworkForTrack,
                                onTrackLongPress = { chartTrack ->
                                    buildShareText(chartTrack)?.let { text ->
                                        clipboardManager.setText(AnnotatedString(text))
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Copied to clipboard")
                                        }
                                    }
                                },
                                onTrackShare = if (shareManager.hasNativeShare) {
                                    { chartTrack ->
                                        buildShareText(chartTrack)?.let { shareManager.nativeShare(it) }
                                    }
                                } else {
                                    null
                                },
                            )
                        }
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
