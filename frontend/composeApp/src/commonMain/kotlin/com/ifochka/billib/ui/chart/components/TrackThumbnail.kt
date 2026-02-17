package com.ifochka.billib.ui.chart.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ifochka.billib.data.model.Track

@Composable
fun TrackThumbnail(
    track: Track,
    modifier: Modifier = Modifier,
) {
    val artworkUrl = track.artworkUrl

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (!artworkUrl.isNullOrBlank()) {
            // Display album artwork using Coil (performant for lists)
            AsyncImage(
                model = artworkUrl,
                contentDescription = "${track.artistName} - ${track.title}",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            // No artwork URL, show music note placeholder
            MusicNotePlaceholder(track)
        }
    }
}

@Composable
private fun MusicNotePlaceholder(track: Track) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = track.title,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
