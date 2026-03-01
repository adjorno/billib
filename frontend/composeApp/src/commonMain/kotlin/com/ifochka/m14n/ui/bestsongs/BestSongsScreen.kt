package com.ifochka.m14n.ui.bestsongs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.ChartTrack
import com.ifochka.m14n.ui.bestsongs.components.BestSongsFilterBar
import com.ifochka.m14n.ui.chart.components.ChartTrackList
import com.ifochka.m14n.ui.chart.components.PositionBadge
import com.ifochka.m14n.ui.chart.components.SkeletonChartTrackItem
import com.ifochka.m14n.ui.shared.SkeletonBox
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BestSongsScreen(
    onTrackClick: (Long) -> Unit = {},
    viewModel: BestSongsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun buildTrackText(chartTrack: ChartTrack): String? {
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
            is BestSongsUiState.Loading -> {
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
                    LazyColumn(modifier = Modifier.weight(1f)) { skeletonTrackItems() }
                }
            }

            is BestSongsUiState.Success -> {
                val chartTracks = state.tracks.mapIndexed { index, track ->
                    ChartTrack(
                        track = track,
                        rank = index + 1,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    BestSongsFilterBar(
                        availableCharts = state.availableCharts,
                        filter = state.filter,
                        onFilterChanged = { viewModel.applyFilter(it) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    if (state.isLoadingTracks) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) { skeletonTrackItems() }
                    } else {
                        ChartTrackList(
                            chartTracks = chartTracks,
                            onArtworkNeeded = viewModel::loadArtworkForTrack,
                            onTrackClick = onTrackClick,
                            badgeFor = { PositionBadge.None },
                            onTrackLongPress = { chartTrack ->
                                buildTrackText(chartTrack)?.let { text ->
                                    clipboardManager.setText(AnnotatedString(text))
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Copied to clipboard")
                                    }
                                }
                            },
                            onTrackShare = null,
                        )
                    }
                }
            }

            is BestSongsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                        )
                        Button(onClick = { viewModel.load() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.skeletonTrackItems(count: Int = 10) {
    items(count) { SkeletonChartTrackItem() }
}
