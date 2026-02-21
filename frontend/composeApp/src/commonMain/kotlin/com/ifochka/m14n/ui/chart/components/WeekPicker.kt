package com.ifochka.m14n.ui.chart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ifochka.m14n.data.util.DateUtils
import com.ifochka.m14n.ui.chart.WeekDirection
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WeekPicker(
    weekDate: String,
    chartStartDate: String?,
    chartEndDate: String?,
    onOpenDateInput: () -> Unit,
    onNavigate: (WeekDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Calculate boundary conditions
    val currentDate = DateUtils.parseChartDate(weekDate)
    val chartStart = chartStartDate?.let { DateUtils.parseChartDate(it) }
    val chartEnd = chartEndDate?.let { DateUtils.parseChartDate(it) }
    val today = DateUtils.getToday()

    val nextWeek = currentDate?.let { DateUtils.getNextWeek(it) }

    val isFirst = currentDate != null && chartStart != null && currentDate <= chartStart
    val isLatest = (currentDate != null && chartEnd != null && currentDate >= chartEnd) ||
        (nextWeek != null && chartEnd == null && nextWeek > today)

    val canGoBack = !isFirst && currentDate != null
    val canGoForward = !isLatest && currentDate != null

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous week button
        IconButton(
            onClick = { onNavigate(WeekDirection.PREVIOUS) },
            enabled = canGoBack,
            modifier = Modifier.alpha(if (isFirst) 0f else 1f),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous week",
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center: Current week display (clickable to open date input)
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                onClick = onOpenDateInput,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Week of $weekDate",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Badge for Latest or First
            if (isLatest || isFirst) {
                Surface(
                    color = if (isLatest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    shape = CircleShape,
                    modifier = Modifier
                        .offset(x = (-8).dp, y = (-2).dp),
                ) {
                    Text(
                        text = if (isLatest) "Latest" else "First",
                        color = if (isLatest) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSecondary
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Next week button
        IconButton(
            onClick = { onNavigate(WeekDirection.NEXT) },
            enabled = canGoForward,
            modifier = Modifier.alpha(if (isLatest) 0f else 1f),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next week",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview
@Composable
fun WeekPickerFirstPreview() {
    MaterialTheme {
        WeekPicker(
            weekDate = "2024-01-01",
            chartStartDate = "2024-01-01",
            chartEndDate = "2024-03-01",
            onOpenDateInput = {},
            onNavigate = {},
        )
    }
}

@Preview
@Composable
fun WeekPickerMiddlePreview() {
    MaterialTheme {
        WeekPicker(
            weekDate = "2024-02-01",
            chartStartDate = "2024-01-01",
            chartEndDate = "2024-03-01",
            onOpenDateInput = {},
            onNavigate = {},
        )
    }
}

@Preview
@Composable
fun WeekPickerLatestStringPreview() {
    MaterialTheme {
        WeekPicker(
            weekDate = "latest",
            chartStartDate = "2024-01-01",
            chartEndDate = "2024-03-01",
            onOpenDateInput = {},
            onNavigate = {},
        )
    }
}

@Preview
@Composable
fun WeekPickerLatestPreview() {
    MaterialTheme {
        WeekPicker(
            weekDate = "2024-03-01",
            chartStartDate = "2024-01-01",
            chartEndDate = "2024-03-01",
            onOpenDateInput = {},
            onNavigate = {},
        )
    }
}
