package com.ifochka.m14n.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.Track
import com.ifochka.m14n.data.model.TrendList
import com.ifochka.m14n.ui.shared.SkeletonBox

@Composable
fun TrendSection(
    trendList: TrendList,
    onArtworkNeeded: suspend (Track) -> Unit,
    onTrackClick: (Long) -> Unit = {},
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (isLoading) {
            SkeletonBox(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(0.4f)
                    .height(20.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(5) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SkeletonBox(modifier = Modifier.size(80.dp))
                        SkeletonBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
                        SkeletonBox(modifier = Modifier.size(width = 60.dp, height = 12.dp))
                    }
                }
            }
        } else {
            Text(
                text = trendList.name ?: "",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(trendList.tracks ?: emptyList()) { track ->
                    Box(modifier = Modifier.clickable { track.id?.let(onTrackClick) }) {
                        TrendTrackItem(
                            track = track,
                            onArtworkNeeded = onArtworkNeeded,
                        )
                    }
                }
            }
        }
    }
}
