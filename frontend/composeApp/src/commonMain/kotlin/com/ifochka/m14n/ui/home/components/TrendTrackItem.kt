package com.ifochka.m14n.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ifochka.m14n.data.model.Artist
import com.ifochka.m14n.data.model.Track

private val ItemSize = 80.dp
private val CardCornerRadius = 12.dp
private val FallbackIconSize = 32.dp

@Composable
fun TrendTrackItem(
    track: Track,
    onArtworkNeeded: suspend (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val artist = track.artistName ?: track.artist?.name ?: ""
    LaunchedEffect(track.id) { onArtworkNeeded(track) }
    Column(
        modifier = modifier.width(ItemSize),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Card(shape = RoundedCornerShape(CardCornerRadius)) {
            Box(modifier = Modifier.size(ItemSize)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                if (!track.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = track.artworkUrl,
                        contentDescription = "$artist - ${track.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(FallbackIconSize).align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            text = track.title ?: "",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = artist,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
