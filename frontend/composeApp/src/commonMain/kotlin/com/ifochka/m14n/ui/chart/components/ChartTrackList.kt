package com.ifochka.m14n.ui.chart.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ifochka.m14n.data.model.ChartTrack
import com.ifochka.m14n.data.model.Track
import kotlin.math.abs

@Composable
fun ChartTrackList(
    chartTracks: List<ChartTrack>,
    onArtworkNeeded: suspend (Track) -> Unit,
    onTrackLongPress: (ChartTrack) -> Unit,
    onTrackShare: ((ChartTrack) -> Unit)?,
    onTrackClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    badgeFor: (ChartTrack) -> PositionBadge = { chartTrack ->
        when {
            chartTrack.isDebut -> PositionBadge.New
            chartTrack.isUp -> PositionBadge.Up(change = abs(chartTrack.rankChange))
            chartTrack.isDown -> PositionBadge.Down(change = abs(chartTrack.rankChange))
            else -> PositionBadge.Same
        }
    },
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = chartTracks,
            key = { it.track?.id ?: it.rank },
            contentType = { "track" },
        ) { chartTrack ->
            ChartTrackItem(
                chartTrack = chartTrack,
                badge = badgeFor(chartTrack),
                onArtworkNeeded = onArtworkNeeded,
                onClick = { chartTrack.track?.id?.let(onTrackClick) },
                onLongPress = { onTrackLongPress(chartTrack) },
                onShare = onTrackShare?.let { { it(chartTrack) } },
            )
            HorizontalDivider()
        }
    }
}
