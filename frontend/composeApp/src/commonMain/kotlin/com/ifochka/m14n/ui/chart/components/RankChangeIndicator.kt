package com.ifochka.m14n.ui.chart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ifochka.m14n.data.model.ChartTrack
import kotlin.math.abs

@Composable
fun RankChangeIndicator(
    chartTrack: ChartTrack,
    modifier: Modifier = Modifier,
) {
    when {
        chartTrack.isDebut -> {
            // Show "NEW" badge for debut tracks
            Box(
                modifier = modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "NEW",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        chartTrack.isUp -> {
            // Green up arrow with change amount
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Up",
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = abs(chartTrack.rankChange).toString(),
                    fontSize = 12.sp,
                    color = Color(0xFF00C853),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        chartTrack.isDown -> {
            // Red down arrow with change amount
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Down",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = abs(chartTrack.rankChange).toString(),
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        else -> {
            // No change - show dash
            Text(
                text = "—",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier,
            )
        }
    }
}
