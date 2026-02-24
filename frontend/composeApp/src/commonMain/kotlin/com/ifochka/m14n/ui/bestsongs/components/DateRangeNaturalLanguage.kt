package com.ifochka.m14n.ui.bestsongs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.ui.bestsongs.BestSongsFilter
import com.ifochka.m14n.ui.bestsongs.DateRange

private fun verbLabel(range: DateRange) =
    when (range) {
        is DateRange.AllTime -> "— all time"
        is DateRange.LastPeriod -> "for the last"
        is DateRange.SinceForDuration -> "since"
        is DateRange.CustomRange -> "from \u2026 to"
    }

private fun DateRange.LastPeriod.durationLabel(): String {
    val parts = buildList {
        if (years > 0) add("${years}yr")
        if (months > 0) add("${months}mo")
        if (days > 0) add("${days}d")
    }
    return parts.joinToString(" ").ifEmpty { "\u2026" }
}

private const val YEAR_DIGIT_COUNT = 4

private fun DateRange.SinceForDuration.fromDateLabel() = fromDate.take(YEAR_DIGIT_COUNT)

private fun DateRange.SinceForDuration.durationLabel(): String {
    val parts = buildList {
        if (durationYears > 0) add("${durationYears}yr")
        if (durationMonths > 0) add("${durationMonths}mo")
    }
    return parts.joinToString(" ").ifEmpty { "\u2026" }
}

private val LAST_PERIOD_OPTIONS = listOf(
    DateRange.LastPeriod(months = 3) to "3 months",
    DateRange.LastPeriod(months = 6) to "6 months",
    DateRange.LastPeriod(years = 1) to "1 year",
    DateRange.LastPeriod(years = 3) to "3 years",
    DateRange.LastPeriod(years = 5) to "5 years",
)

private val SINCE_YEAR_OPTIONS = listOf(
    "2000" to "2000-01-01",
    "2010" to "2010-01-01",
    "2020" to "2020-01-01",
)

private val SINCE_DURATION_OPTIONS = listOf(1 to "1 year", 5 to "5 years", 10 to "10 years")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DateRangeNaturalLanguage(
    availableCharts: List<Chart>,
    filter: BestSongsFilter,
    onFilterChanged: (BestSongsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showChartMenu by remember { mutableStateOf(false) }
    var showVerbMenu by remember { mutableStateOf(false) }
    var showDurationMenu by remember { mutableStateOf(false) }
    var showSinceFromMenu by remember { mutableStateOf(false) }
    var showDurationDialog by remember { mutableStateOf(false) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    FlowRow(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = "Best", style = MaterialTheme.typography.bodyLarge)

        Box {
            UnderlineButton(text = filter.selectedChart.name ?: "chart") { showChartMenu = true }
            DropdownMenu(expanded = showChartMenu, onDismissRequest = { showChartMenu = false }) {
                availableCharts.forEach { chart ->
                    DropdownMenuItem(
                        text = { Text(chart.name ?: "") },
                        onClick = {
                            onFilterChanged(filter.copy(selectedChart = chart))
                            showChartMenu = false
                        },
                    )
                }
            }
        }

        Text(text = "songs", style = MaterialTheme.typography.bodyLarge)

        Box {
            UnderlineButton(text = verbLabel(filter.dateRange)) { showVerbMenu = true }
            DropdownMenu(expanded = showVerbMenu, onDismissRequest = { showVerbMenu = false }) {
                DropdownMenuItem(text = { Text("— all time") }, onClick = {
                    onFilterChanged(filter.copy(dateRange = DateRange.AllTime))
                    showVerbMenu = false
                })
                DropdownMenuItem(text = { Text("for the last") }, onClick = {
                    if (filter.dateRange !is DateRange.LastPeriod) {
                        onFilterChanged(filter.copy(dateRange = DateRange.LastPeriod(months = 3)))
                    }
                    showVerbMenu = false
                })
                DropdownMenuItem(text = { Text("since") }, onClick = {
                    if (filter.dateRange !is DateRange.SinceForDuration) {
                        onFilterChanged(
                            filter.copy(
                                dateRange = DateRange.SinceForDuration(
                                    fromDate = "2000-01-01",
                                    durationYears = 10,
                                ),
                            ),
                        )
                    }
                    showVerbMenu = false
                })
                DropdownMenuItem(text = { Text("from \u2026 to") }, onClick = {
                    if (filter.dateRange !is DateRange.CustomRange) {
                        onFilterChanged(filter.copy(dateRange = DateRange.CustomRange(from = null, to = null)))
                    }
                    showVerbMenu = false
                })
            }
        }

        when (val range = filter.dateRange) {
            is DateRange.AllTime -> Unit
            is DateRange.LastPeriod -> {
                Box {
                    UnderlineButton(text = range.durationLabel()) { showDurationMenu = true }
                    DropdownMenu(
                        expanded = showDurationMenu,
                        onDismissRequest = { showDurationMenu = false },
                    ) {
                        LAST_PERIOD_OPTIONS.forEach { (period, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                onFilterChanged(filter.copy(dateRange = period))
                                showDurationMenu = false
                            })
                        }
                        DropdownMenuItem(text = { Text("Custom\u2026") }, onClick = {
                            showDurationMenu = false
                            showDurationDialog = true
                        })
                    }
                }
            }
            is DateRange.SinceForDuration -> {
                Box {
                    UnderlineButton(text = range.fromDateLabel()) { showSinceFromMenu = true }
                    DropdownMenu(
                        expanded = showSinceFromMenu,
                        onDismissRequest = { showSinceFromMenu = false },
                    ) {
                        SINCE_YEAR_OPTIONS.forEach { (label, date) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                onFilterChanged(filter.copy(dateRange = range.copy(fromDate = date)))
                                showSinceFromMenu = false
                            })
                        }
                        DropdownMenuItem(text = { Text("Pick a date\u2026") }, onClick = {
                            showSinceFromMenu = false
                            showFromPicker = true
                        })
                    }
                }
                Text(text = "for", style = MaterialTheme.typography.bodyLarge)
                Box {
                    UnderlineButton(text = range.durationLabel()) { showDurationMenu = true }
                    DropdownMenu(
                        expanded = showDurationMenu,
                        onDismissRequest = { showDurationMenu = false },
                    ) {
                        SINCE_DURATION_OPTIONS.forEach { (years, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                onFilterChanged(
                                    filter.copy(
                                        dateRange = range.copy(
                                            durationYears = years,
                                            durationMonths = 0,
                                        ),
                                    ),
                                )
                                showDurationMenu = false
                            })
                        }
                        DropdownMenuItem(text = { Text("Custom\u2026") }, onClick = {
                            showDurationMenu = false
                            showDurationDialog = true
                        })
                    }
                }
            }
            is DateRange.CustomRange -> {
                UnderlineButton(text = "from: ${range.from ?: "any"}") { showFromPicker = true }
                UnderlineButton(text = "to: ${range.to ?: "any"}") { showToPicker = true }
            }
        }
    }

    if (showDurationDialog) {
        val range = filter.dateRange
        DurationPickerDialog(
            years = if (range is DateRange.LastPeriod) {
                range.years
            } else if (range is DateRange.SinceForDuration) {
                range.durationYears
            } else {
                0
            },
            months = if (range is DateRange.LastPeriod) {
                range.months
            } else if (range is DateRange.SinceForDuration) {
                range.durationMonths
            } else {
                0
            },
            days = if (range is DateRange.LastPeriod) range.days else 0,
            onConfirm = { y, m, d ->
                val newRange = when (range) {
                    is DateRange.LastPeriod -> DateRange.LastPeriod(years = y, months = m, days = d)
                    is DateRange.SinceForDuration -> range.copy(durationYears = y, durationMonths = m)
                    else -> range
                }
                onFilterChanged(filter.copy(dateRange = newRange))
                showDurationDialog = false
            },
            onDismiss = { showDurationDialog = false },
        )
    }

    if (showFromPicker) {
        val currentDate = when (val range = filter.dateRange) {
            is DateRange.SinceForDuration -> range.fromDate
            is DateRange.CustomRange -> range.from
            else -> null
        }
        BestSongsDatePicker(
            currentDate = currentDate,
            onDateSelected = { date ->
                when (val range = filter.dateRange) {
                    is DateRange.SinceForDuration -> onFilterChanged(
                        filter.copy(dateRange = range.copy(fromDate = date)),
                    )
                    is DateRange.CustomRange -> onFilterChanged(filter.copy(dateRange = range.copy(from = date)))
                    else -> Unit
                }
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false },
        )
    }

    if (showToPicker) {
        (filter.dateRange as? DateRange.CustomRange)?.let { range ->
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
}

@Composable
private fun UnderlineButton(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = TextDecoration.Underline,
        )
    }
}
