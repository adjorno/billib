package com.ifochka.m14n.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ifochka.auth.AuthState
import com.ifochka.core.share.ShareManager
import com.ifochka.m14n.data.model.ChartTrack
import com.ifochka.m14n.data.util.DateUtils
import com.ifochka.m14n.ui.auth.AuthViewModel
import com.ifochka.m14n.ui.auth.SignInBottomSheet
import com.ifochka.m14n.ui.chart.components.ChartTopBar
import com.ifochka.m14n.ui.chart.components.ChartTrackList
import com.ifochka.m14n.ui.chart.components.SkeletonChartTrackItem
import com.ifochka.m14n.ui.shared.SkeletonBox
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChartScreen(
    onTrackClick: (Long) -> Unit = {},
    viewModel: ChartViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var showSignInSheet by remember { mutableStateOf(false) }
    // Anonymous users may view the 3 most recent weeks; anything older requires sign-in.
    val anonCutoff = remember {
        DateUtils.getToday().minus(21, DateTimeUnit.DAY).toString()
    }
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .height(40.dp),
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(10) { SkeletonChartTrackItem() }
                    }
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
                        latestWeekDate = state.latestWeek,
                        onChartSelected = { chartId ->
                            viewModel.selectChart(chartId)
                        },
                        onWeekNavigate = { direction ->
                            if (direction == WeekDirection.PREVIOUS &&
                                authState is AuthState.Anonymous &&
                                state.selectedWeek <= anonCutoff
                            ) {
                                showSignInSheet = true
                            } else {
                                viewModel.navigateWeek(direction)
                            }
                        },
                        onWeekSelect = { weekDate ->
                            if (authState is AuthState.Anonymous && weekDate <= anonCutoff) {
                                showSignInSheet = true
                            } else {
                                viewModel.selectWeek(
                                    chartId = state.selectedChart.id ?: return@ChartTopBar,
                                    weekDate = weekDate,
                                )
                            }
                        },
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    key(state.selectedChart.id, state.selectedWeek) {
                        state.chartList.chartTracks?.let { tracks ->
                            ChartTrackList(
                                chartTracks = tracks,
                                onArtworkNeeded = viewModel::loadArtworkForTrack,
                                onTrackClick = onTrackClick,
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

        if (showSignInSheet) {
            SignInBottomSheet(
                onDismiss = { showSignInSheet = false },
                onSuccess = { showSignInSheet = false },
            )
        }
    }
}
