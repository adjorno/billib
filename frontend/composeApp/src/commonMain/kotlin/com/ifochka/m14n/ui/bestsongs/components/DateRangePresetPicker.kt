package com.ifochka.m14n.ui.bestsongs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.ui.bestsongs.BestSongsFilter
import com.ifochka.m14n.ui.bestsongs.DateRange

private val PRESET_RANGES = listOf(
    DateRange.AllTime to "All Time",
    DateRange.LastPeriod(months = 3) to "Last 3M",
    DateRange.LastPeriod(years = 1) to "Last 1Y",
    DateRange.LastPeriod(years = 5) to "Last 5Y",
    DateRange.SinceForDuration(fromDate = "2000-01-01", durationYears = 10) to "2000s",
    DateRange.SinceForDuration(fromDate = "2010-01-01", durationYears = 10) to "2010s",
    DateRange.CustomRange(from = null, to = null) to "Custom \u25b8",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePresetPicker(
    filter: BestSongsFilter,
    onFilterChanged: (BestSongsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(items = PRESET_RANGES, key = { (_, label) -> label }) { (range, label) ->
            val isSelected = when {
                range is DateRange.CustomRange -> filter.dateRange is DateRange.CustomRange
                else -> range == filter.dateRange
            }
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (range is DateRange.CustomRange) {
                        showCustomDialog = true
                    } else {
                        onFilterChanged(filter.copy(dateRange = range))
                    }
                },
                label = { Text(label) },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }

    if (showCustomDialog) {
        CustomRangeDialog(
            current = filter.dateRange as? DateRange.CustomRange
                ?: DateRange.CustomRange(from = null, to = null),
            onConfirm = { range ->
                onFilterChanged(filter.copy(dateRange = range))
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangeDialog(
    current: DateRange.CustomRange,
    onConfirm: (DateRange.CustomRange) -> Unit,
    onDismiss: () -> Unit,
) {
    var from by remember { mutableStateOf(current.from) }
    var to by remember { mutableStateOf(current.to) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Range") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showFromPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("From: ${from ?: "Any"}")
                }
                OutlinedButton(
                    onClick = { showToPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("To: ${to ?: "Any"}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(DateRange.CustomRange(from = from, to = to)) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )

    if (showFromPicker) {
        BestSongsDatePicker(
            currentDate = from,
            onDateSelected = { from = it },
            onDismiss = { showFromPicker = false },
        )
    }

    if (showToPicker) {
        BestSongsDatePicker(
            currentDate = to,
            onDateSelected = { to = it },
            onDismiss = { showToPicker = false },
        )
    }
}
