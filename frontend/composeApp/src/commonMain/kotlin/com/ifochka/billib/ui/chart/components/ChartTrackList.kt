package com.ifochka.billib.ui.chart.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ifochka.billib.data.model.ChartTrack
import com.ifochka.billib.data.model.Track

@Composable
fun ChartTrackList(
    chartTracks: List<ChartTrack>,
    onArtworkNeeded: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = chartTracks,
            key = { it.track?.id ?: it.rank },
            contentType = { "track" },
        ) { chartTrack ->
            ChartTrackItem(chartTrack = chartTrack, onArtworkNeeded = onArtworkNeeded)
            HorizontalDivider()
        }
    }
}
