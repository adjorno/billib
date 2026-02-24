package com.ifochka.m14n.ui.bestsongs.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.util.DateUtils
import com.ifochka.m14n.ui.bestsongs.BestSongsFilter
import com.ifochka.m14n.ui.chart.components.ChartSelectorRow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val STYLE_COUNT = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestSongsFilterBar(
    availableCharts: List<Chart>,
    filter: BestSongsFilter,
    onFilterChanged: (BestSongsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedStyle by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        AnimatedVisibility(visible = selectedStyle == 0) {
            Column {
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
            }
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            SegmentedButton(
                selected = selectedStyle == 0,
                onClick = { selectedStyle = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = STYLE_COUNT),
                label = { Text("A") },
            )
            SegmentedButton(
                selected = selectedStyle == 1,
                onClick = { selectedStyle = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = STYLE_COUNT),
                label = { Text("B") },
            )
        }

        HorizontalDivider()

        Crossfade(targetState = selectedStyle) { style ->
            when (style) {
                0 -> DateRangeModeSwitcher(
                    filter = filter,
                    onFilterChanged = onFilterChanged,
                    modifier = Modifier.fillMaxWidth(),
                )
                else -> DateRangeNaturalLanguage(
                    availableCharts = availableCharts,
                    filter = filter,
                    onFilterChanged = onFilterChanged,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
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
