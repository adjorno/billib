package com.ifochka.m14n.ui.chart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.ui.theme.M14nTheme

@Composable
fun ChartPositionBadge(
    rank: Int,
    badge: PositionBadge,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (rank <= 10) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        RankChangeIndicator(badge = badge)
    }
}

@Preview
@Composable
fun ChartPositionBadgeTopTenUpPreview() {
    M14nTheme {
        ChartPositionBadge(rank = 1, badge = PositionBadge.Up(change = 3))
    }
}

@Preview
@Composable
fun ChartPositionBadgeDebutPreview() {
    M14nTheme {
        ChartPositionBadge(rank = 42, badge = PositionBadge.New)
    }
}

@Preview
@Composable
fun ChartPositionBadgeDownPreview() {
    M14nTheme {
        ChartPositionBadge(rank = 15, badge = PositionBadge.Down(change = 5))
    }
}

@Preview
@Composable
fun ChartPositionBadgeSamePreview() {
    M14nTheme {
        ChartPositionBadge(rank = 7, badge = PositionBadge.Same)
    }
}
