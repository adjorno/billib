package com.ifochka.m14n.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.ChartTrack
import com.ifochka.m14n.ui.chart.components.ChartTrackList
import com.ifochka.m14n.ui.chart.components.SkeletonChartTrackItem
import com.ifochka.m14n.ui.search.components.SearchTextField
import com.ifochka.m14n.ui.shared.ArtistChip
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onTrackClick: (Long) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            SearchTextField(
                query = query,
                onQueryChange = viewModel::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalDivider()
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Search by artist or track title",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                is SearchUiState.Loading -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(8) { SkeletonChartTrackItem() }
                    }
                }

                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is SearchUiState.Results -> {
                    if (state.artists.isNotEmpty()) {
                        Text(
                            text = "Artists",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                        FlowRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                            state.artists.forEach { artist ->
                                ArtistChip(
                                    artist = artist,
                                    artworkUrl = null,
                                    onClick = {
                                        artist.id?.let(onArtistClick)
                                            ?: viewModel.setQuery(artist.name ?: "")
                                    },
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = "Tracks (${state.tracksTotal})",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    if (state.tracks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No tracks found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        val chartTracks = state.tracks.mapIndexed { index, track ->
                            ChartTrack(track = track, rank = index + 1)
                        }
                        ChartTrackList(
                            chartTracks = chartTracks,
                            onArtworkNeeded = viewModel::loadArtworkForTrack,
                            onTrackClick = onTrackClick,
                            onTrackLongPress = { chartTrack ->
                                val track = chartTrack.track ?: return@ChartTrackList
                                val artist = track.artist?.name ?: track.artistName ?: "Unknown Artist"
                                val title = track.title ?: "Unknown Title"
                                clipboardManager.setText(AnnotatedString("$artist \u2014 $title"))
                            },
                            onTrackShare = null,
                        )
                    }
                }
            }
        }
    }
}
