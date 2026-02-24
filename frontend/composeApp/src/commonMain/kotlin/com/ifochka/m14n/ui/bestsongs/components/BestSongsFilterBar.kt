package com.ifochka.m14n.ui.bestsongs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.util.DateUtils
import com.ifochka.m14n.ui.bestsongs.BestSongsFilter
import com.ifochka.m14n.ui.bestsongs.DateRange
import com.ifochka.m14n.ui.chart.components.ChartSelectorRow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val DATE_RANGES = listOf(
    DateRange.AllTime to "All Time",
    DateRange.LastPeriod(months = 3) to "Last 3M",
    DateRange.LastPeriod(years = 1) to "Last Year",
    DateRange.SinceForDuration(fromDate = "2000-01-01", durationYears = 10) to "2000s",
    DateRange.SinceForDuration(fromDate = "2010-01-01", durationYears = 10) to "2010s",
    DateRange.SinceForDuration(fromDate = "2020-01-01", durationYears = 5) to "2020s",
    DateRange.CustomRange(from = null, to = null) to "Custom",
)

@Composable
fun BestSongsFilterBar(
    availableCharts: List<Chart>,
    filter: BestSongsFilter,
    onFilterChanged: (BestSongsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ChartSelectorRow(
            availableCharts = availableCharts,
            selectedChart = filter.selectedChart,
            onChartSelected = { chartId ->
                availableCharts.find { it.id == chartId }?.let {
                    onFilterChanged(filter.copy(selectedChart = it))
                }
            },
        )

        HorizontalDivider()

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = DATE_RANGES, key = { (_, label) -> label }) { (range, label) ->
                val isSelected = when {
                    range is DateRange.CustomRange -> filter.dateRange is DateRange.CustomRange
                    else -> range == filter.dateRange
                }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newRange = if (range is DateRange.CustomRange) {
                            (filter.dateRange as? DateRange.CustomRange) ?: DateRange.CustomRange(
                                from = null,
                                to = null,
                            )
                        } else {
                            range
                        }
                        onFilterChanged(filter.copy(dateRange = newRange))
                    },
                    label = { Text(label) },
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }

        if (filter.dateRange is DateRange.CustomRange) {
            val custom = filter.dateRange
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = { showFromPicker = true }) {
                    Text("From: ${custom.from ?: "Any"}")
                }
                OutlinedButton(onClick = { showToPicker = true }) {
                    Text("To: ${custom.to ?: "Any"}")
                }
            }
        }
    }

    if (showFromPicker) {
        BestSongsDatePicker(
            currentDate = (filter.dateRange as? DateRange.CustomRange)?.from,
            onDateSelected = { date ->
                val current = filter.dateRange as? DateRange.CustomRange
                    ?: DateRange.CustomRange(from = null, to = null)
                onFilterChanged(filter.copy(dateRange = current.copy(from = date)))
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false },
        )
    }

    if (showToPicker) {
        BestSongsDatePicker(
            currentDate = (filter.dateRange as? DateRange.CustomRange)?.to,
            onDateSelected = { date ->
                val current = filter.dateRange as? DateRange.CustomRange
                    ?: DateRange.CustomRange(from = null, to = null)
                onFilterChanged(filter.copy(dateRange = current.copy(to = date)))
                showToPicker = false
            },
            onDismiss = { showToPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
internal fun BestSongsDatePicker(
    currentDate: String?,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = currentDate
        ?.let { DateUtils.parseChartDate(it) }
        ?.atStartOfDayIn(TimeZone.UTC)
        ?.toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant
                            .fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC)
                            .date
                        onDateSelected(DateUtils.formatChartDate(date))
                        onDismiss()
                    }
                },
                enabled = datePickerState.selectedDateMillis != null,
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
