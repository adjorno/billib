package com.ifochka.m14n.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ifochka.m14n.data.model.Artist
import com.ifochka.m14n.data.model.Track

@Composable
fun TrendTrackItem(
    track: Track,
    onArtworkNeeded: suspend (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val artist = track.artistName ?: track.artist?.name ?: ""
    LaunchedEffect(track.id) { onArtworkNeeded(track) }
    Card(
        modifier = modifier.width(80.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(modifier = Modifier.width(80.dp).height(120.dp)) {
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
                    modifier = Modifier.size(32.dp).align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        ),
                    )
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = track.title ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
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
