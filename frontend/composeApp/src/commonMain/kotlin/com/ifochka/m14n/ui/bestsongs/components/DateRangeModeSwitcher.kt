package com.ifochka.m14n.ui.bestsongs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.ui.bestsongs.BestSongsFilter
import com.ifochka.m14n.ui.bestsongs.DateRange

private enum class AMode(
    val label: String,
) {
    AllTime("All Time"),
    Last("Last"),
    Since("Since"),
    Range("Range"),
}

private val LAST_PERIOD_CHIPS = listOf(
    DateRange.LastPeriod(months = 3) to "3M",
    DateRange.LastPeriod(months = 6) to "6M",
    DateRange.LastPeriod(years = 1) to "1Y",
    DateRange.LastPeriod(years = 3) to "3Y",
    DateRange.LastPeriod(years = 5) to "5Y",
)

private val SINCE_PRESETS = listOf(
    DateRange.SinceForDuration(fromDate = "2000-01-01", durationYears = 10) to "2000 \u2022 10Y",
    DateRange.SinceForDuration(fromDate = "2010-01-01", durationYears = 10) to "2010 \u2022 10Y",
    DateRange.SinceForDuration(fromDate = "2020-01-01", durationYears = 5) to "2020 \u2022 5Y",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeModeSwitcher(
    filter: BestSongsFilter,
    onFilterChanged: (BestSongsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mode = when (filter.dateRange) {
        is DateRange.AllTime -> AMode.AllTime
        is DateRange.LastPeriod -> AMode.Last
        is DateRange.SinceForDuration -> AMode.Since
        is DateRange.CustomRange -> AMode.Range
    }

    var showLastCustom by remember { mutableStateOf(false) }
    var showSinceCustom by remember { mutableStateOf(false) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(AMode.entries, key = { it.name }) { m ->
                FilterChip(
                    selected = m == mode,
                    onClick = {
                        when (m) {
                            AMode.AllTime -> onFilterChanged(filter.copy(dateRange = DateRange.AllTime))
                            AMode.Last -> if (mode != AMode.Last) {
                                onFilterChanged(
                                    filter.copy(dateRange = DateRange.LastPeriod(months = 3)),
                                )
                            }
                            AMode.Since -> if (mode != AMode.Since) {
                                onFilterChanged(
                                    filter.copy(
                                        dateRange = DateRange.SinceForDuration(
                                            fromDate = "2000-01-01",
                                            durationYears = 10,
                                        ),
                                    ),
                                )
                            }
                            AMode.Range -> if (mode != AMode.Range) {
                                onFilterChanged(
                                    filter.copy(dateRange = DateRange.CustomRange(from = null, to = null)),
                                )
                            }
                        }
                    },
                    label = { Text(m.label) },
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (mode) {
                AMode.AllTime -> Unit
                AMode.Last -> {
                    items(LAST_PERIOD_CHIPS, key = { it.second }) { (period, label) ->
                        FilterChip(
                            selected = filter.dateRange == period,
                            onClick = { onFilterChanged(filter.copy(dateRange = period)) },
                            label = { Text(label) },
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                    item(key = "last_custom") {
                        val isCustom = filter.dateRange is DateRange.LastPeriod &&
                            LAST_PERIOD_CHIPS.none { it.first == filter.dateRange }
                        FilterChip(
                            selected = isCustom,
                            onClick = { showLastCustom = true },
                            label = { Text("Custom\u2026") },
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
                AMode.Since -> {
                    items(SINCE_PRESETS, key = { it.second }) { (range, label) ->
                        FilterChip(
                            selected = filter.dateRange == range,
                            onClick = { onFilterChanged(filter.copy(dateRange = range)) },
                            label = { Text(label) },
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                    item(key = "since_custom") {
                        val isCustom = filter.dateRange is DateRange.SinceForDuration &&
                            SINCE_PRESETS.none { it.first == filter.dateRange }
                        FilterChip(
                            selected = isCustom,
                            onClick = { showSinceCustom = true },
                            label = { Text("Custom\u2026") },
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
                AMode.Range -> {
                    val range = filter.dateRange as? DateRange.CustomRange
                        ?: DateRange.CustomRange(from = null, to = null)
                    item(key = "from") {
                        OutlinedButton(
                            onClick = { showFromPicker = true },
                            modifier = Modifier.padding(vertical = 4.dp),
                        ) { Text("From: ${range.from ?: "Any"}") }
                    }
                    item(key = "to") {
                        OutlinedButton(
                            onClick = { showToPicker = true },
                            modifier = Modifier.padding(vertical = 4.dp),
                        ) { Text("To: ${range.to ?: "Any"}") }
                    }
                }
            }
        }
    }

    if (showLastCustom) {
        val current = filter.dateRange as? DateRange.LastPeriod ?: DateRange.LastPeriod()
        DurationPickerDialog(
            years = current.years,
            months = current.months,
            days = current.days,
            onConfirm = { y, m, d ->
                onFilterChanged(filter.copy(dateRange = DateRange.LastPeriod(years = y, months = m, days = d)))
                showLastCustom = false
            },
            onDismiss = { showLastCustom = false },
        )
    }

    if (showSinceCustom) {
        val current = filter.dateRange as? DateRange.SinceForDuration
            ?: DateRange.SinceForDuration(fromDate = "2000-01-01", durationYears = 10)
        SinceCustomDialog(
            current = current,
            onConfirm = { range ->
                onFilterChanged(filter.copy(dateRange = range))
                showSinceCustom = false
            },
            onDismiss = { showSinceCustom = false },
        )
    }

    if (showFromPicker) {
        val range = filter.dateRange as? DateRange.CustomRange ?: DateRange.CustomRange(null, null)
        BestSongsDatePicker(
            currentDate = range.from,
            onDateSelected = { date ->
                onFilterChanged(filter.copy(dateRange = range.copy(from = date)))
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false },
        )
    }

    if (showToPicker) {
        val range = filter.dateRange as? DateRange.CustomRange ?: DateRange.CustomRange(null, null)
        BestSongsDatePicker(
            currentDate = range.to,
            onDateSelected = { date ->
                onFilterChanged(filter.copy(dateRange = range.copy(to = date)))
                showToPicker = false
            },
            onDismiss = { showToPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SinceCustomDialog(
    current: DateRange.SinceForDuration,
    onConfirm: (DateRange.SinceForDuration) -> Unit,
    onDismiss: () -> Unit,
) {
    var fromDate by remember { mutableStateOf(current.fromDate) }
    var durationYears by remember { mutableIntStateOf(current.durationYears) }
    var durationMonths by remember { mutableIntStateOf(current.durationMonths) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Since\u2026") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("From: $fromDate") }
                Text(text = "Duration", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    NumberColumn(label = "Years", value = durationYears, max = 99, onValueChange = {
                        durationYears = it
                    })
                    NumberColumn(label = "Months", value = durationMonths, max = 11, onValueChange = {
                        durationMonths =
                            it
                    })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        DateRange.SinceForDuration(
                            fromDate = fromDate,
                            durationYears = durationYears,
                            durationMonths = durationMonths,
                        ),
                    )
                },
                enabled = durationYears > 0 || durationMonths > 0,
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        BestSongsDatePicker(
            currentDate = fromDate,
            onDateSelected = { fromDate = it },
            onDismiss = { showDatePicker = false },
        )
    }
}
