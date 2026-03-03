package com.ifochka.m14n.ui.artistdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ifochka.m14n.data.model.ChartTrack
import com.ifochka.m14n.data.model.Track
import com.ifochka.m14n.ui.chart.components.ChartTrackItem
import com.ifochka.m14n.ui.chart.components.PositionBadge
import com.ifochka.m14n.ui.chart.components.SkeletonChartTrackItem
import com.ifochka.m14n.ui.shared.ArtistChip
import com.ifochka.m14n.ui.shared.SkeletonBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailsScreen(
    viewModel: ArtistDetailsViewModel,
    onBack: () -> Unit,
    onTrackClick: (Long) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val title = (uiState as? ArtistDetailsUiState.Success)?.artist?.name ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is ArtistDetailsUiState.Loading -> {
                val skeletonItems = remember { List(5) { it } }
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                        ) {
                            SkeletonBox(modifier = Modifier.fillMaxSize())
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(28.dp))
                            SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(20.dp))
                        }
                    }
                    items(skeletonItems) { SkeletonChartTrackItem() }
                }
            }

            is ArtistDetailsUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(text = state.message, color = MaterialTheme.colorScheme.error) }

            is ArtistDetailsUiState.Success -> ArtistDetailsContent(
                state = state,
                onTrackClick = onTrackClick,
                onArtistClick = onArtistClick,
                onArtworkNeeded = viewModel::loadArtworkForTrack,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArtistDetailsContent(
    state: ArtistDetailsUiState.Success,
    onTrackClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onArtworkNeeded: suspend (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            ArtistHero(
                artistName = state.artist.name ?: "",
                globalRank = state.globalRank,
                artworkUrl = state.artworkUrl,
            )
        }
        item {
            Text(
                text = "Top Tracks",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        itemsIndexed(state.tracks.take(5)) { index, track ->
            ChartTrackItem(
                chartTrack = ChartTrack(track = track, rank = index + 1),
                badge = PositionBadge.None,
                onArtworkNeeded = onArtworkNeeded,
                onClick = { track.id?.let(onTrackClick) },
                onLongPress = {},
                onShare = null,
            )
        }
        item {
            Text(
                text = "Collaborates with",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            if (state.relations.isEmpty()) {
                Text(
                    text = "No collaborations found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            } else {
                FlowRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                    state.relations.forEach { artist ->
                        ArtistChip(
                            artist = artist,
                            artworkUrl = null,
                            onClick = { artist.id?.let(onArtistClick) },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistHero(
    artistName: String,
    globalRank: Long,
    artworkUrl: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        if (!artworkUrl.isNullOrBlank()) {
            AsyncImage(
                model = artworkUrl,
                contentDescription = artistName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center),
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Column {
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (globalRank > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = " Global Rank #$globalRank",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
            }
        }
    }
}
