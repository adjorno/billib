package com.ifochka.m14n.ui.chart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.util.DateUtils
import com.ifochka.m14n.ui.chart.WeekDirection

@Composable
fun WeekPicker(
    weekDate: String,
    chartStartDate: String?,
    onOpenDateInput: () -> Unit,
    onNavigate: (WeekDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Calculate boundary conditions
    val currentDate = DateUtils.parseChartDate(weekDate)
    val chartStart = chartStartDate?.let { DateUtils.parseChartDate(it) }
    val today = DateUtils.getToday()

    val canGoBack = currentDate != null && chartStart != null && currentDate > chartStart
    val canGoForward = currentDate != null && currentDate < today

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous week button
        IconButton(
            onClick = { onNavigate(WeekDirection.PREVIOUS) },
            enabled = canGoBack,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous week",
                tint = if (canGoBack) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center: Current week display (clickable to open date input)
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

        Spacer(modifier = Modifier.width(8.dp))

        // Next week button
        IconButton(
            onClick = { onNavigate(WeekDirection.NEXT) },
            enabled = canGoForward,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next week",
                tint = if (canGoForward) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        }
    }
}
