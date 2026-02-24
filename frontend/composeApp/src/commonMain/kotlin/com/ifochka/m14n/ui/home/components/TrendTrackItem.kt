package com.ifochka.m14n.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.Artist
import com.ifochka.m14n.data.model.Track
import com.ifochka.m14n.ui.chart.components.TrackThumbnail

@Composable
fun TrendTrackItem(
    track: Track,
    onArtworkNeeded: suspend (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(track.id) { onArtworkNeeded(track) }
    Column(modifier = modifier.width(80.dp)) {
        TrackThumbnail(
            track = track,
            modifier = Modifier.size(80.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = track.artistName ?: track.artist?.name ?: "",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = track.title ?: "",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
fun TrendTrackItemWithArtworkPreview() {
    MaterialTheme {
        TrendTrackItem(
            track = Track(
                id = 1L,
                title = "Blinding Lights",
                artistName = "The Weeknd",
                artworkUrl = "https://example.com/artwork.jpg",
            ),
            onArtworkNeeded = {},
        )
    }
}

@Preview
@Composable
fun TrendTrackItemNoArtworkPreview() {
    MaterialTheme {
        TrendTrackItem(
            track = Track(
                id = 2L,
                title = "As It Was",
                artist = Artist(name = "Harry Styles"),
            ),
            onArtworkNeeded = {},
        )
    }
}
