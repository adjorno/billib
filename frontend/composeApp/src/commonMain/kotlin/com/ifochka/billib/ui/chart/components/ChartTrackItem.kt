package com.ifochka.billib.ui.chart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ifochka.billib.data.model.ChartTrack
import com.ifochka.billib.data.model.Track

@Composable
fun ChartTrackItem(
    chartTrack: ChartTrack,
    onArtworkNeeded: suspend (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = chartTrack.track ?: return

    LaunchedEffect(track.id) {
        if (track.artworkUrl.isNullOrBlank()) {
            onArtworkNeeded(track)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Position number
        Text(
            text = chartTrack.rank.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Rank change indicator
        RankChangeIndicator(
            chartTrack = chartTrack,
            modifier = Modifier.width(48.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Track thumbnail
        TrackThumbnail(
            track = track,
            modifier = Modifier.size(56.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Track info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = track.title ?: "Unknown Title",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist?.name ?: track.artistName ?: "Unknown Artist",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
