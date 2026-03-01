package com.ifochka.m14n.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.DayTrack
import com.ifochka.m14n.data.model.TrendList
import com.ifochka.m14n.ui.home.components.DayTrackWidget
import com.ifochka.m14n.ui.home.components.TrendSection
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onTrackClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { DayTrackWidget(dayTrack = DayTrack(), isLoading = true) }
                items(2) {
                    TrendSection(
                        trendList = TrendList(),
                        isLoading = true,
                        onArtworkNeeded = { _ -> },
                    )
                }
            }
        }

        is HomeUiState.Success -> {
            LaunchedEffect(state.dayTrack.track?.id) {
                viewModel.loadArtworkForDayTrack()
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Box(modifier = Modifier.clickable { state.dayTrack.track?.id?.let(onTrackClick) }) {
                        DayTrackWidget(dayTrack = state.dayTrack)
                    }
                }
                items(state.trends.trendLists ?: emptyList()) { trendList ->
                    TrendSection(
                        trendList = trendList,
                        onArtworkNeeded = viewModel::loadArtworkForTrendTrack,
                        onTrackClick = onTrackClick,
                    )
                }
            }
        }

        is HomeUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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
