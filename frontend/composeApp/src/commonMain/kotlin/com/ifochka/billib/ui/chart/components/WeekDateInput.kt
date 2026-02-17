package com.ifochka.billib.ui.chart.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ifochka.billib.data.util.DateUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Date picker dialog for selecting a chart week.
 * Uses Material3 DatePicker which works across all platforms.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun WeekDateInput(
    currentWeek: String,
    chartStartDate: String?,
    onWeekSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Parse current week to set initial date
    val initialDate = DateUtils.parseChartDate(currentWeek)
    val initialMillis = initialDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant
                            .fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC)
                            .date

                        // Find Monday of the selected week
                        val mondayDate = DateUtils.getMondayOfWeek(selectedDate)

                        // Validate against chart range
                        if (DateUtils.isDateInRange(mondayDate, chartStartDate)) {
                            onWeekSelected(DateUtils.formatChartDate(mondayDate))
                            onDismiss()
                        }
                    }
                },
                enabled = datePickerState.selectedDateMillis != null,
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier,
    ) {
        DatePicker(state = datePickerState)
    }
}
