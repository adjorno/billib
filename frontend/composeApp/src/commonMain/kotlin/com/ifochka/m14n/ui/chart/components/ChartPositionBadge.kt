package com.ifochka.m14n.ui.chart.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.ui.theme.M14nTheme

@Composable
fun ChartPositionBadge(
    rank: Int,
    modifier: Modifier = Modifier,
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
        modifier = modifier.width(40.dp),
    )
}

@Preview
@Composable
fun ChartPositionBadgePreview() {
    M14nTheme {
        ChartPositionBadge(rank = 1)
    }
}

@Preview
@Composable
fun ChartPositionBadgeOutOfTopTenPreview() {
    M14nTheme {
        ChartPositionBadge(rank = 42)
    }
}
