package com.ifochka.billib.ui.chart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.ifochka.billib.data.util.DateUtils

/**
 * Text field for entering a date to navigate to a specific chart week.
 * Supports MM/DD/YYYY format and shows live preview of the week range.
 */
@Composable
fun WeekDateInput(
    currentWeek: String,
    chartStartDate: String?,
    onWeekSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewWeek by remember { mutableStateOf<String?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { newValue ->
                inputText = newValue
                errorMessage = null
                previewWeek = null

                // Parse date and validate
                val parsedDate = parseDateInput(newValue)
                if (parsedDate != null) {
                    val mondayDate = DateUtils.getMondayOfWeek(parsedDate)

                    // Validate against chart range
                    if (DateUtils.isDateInRange(mondayDate, chartStartDate)) {
                        previewWeek = DateUtils.formatWeekRange(mondayDate)
                    } else {
                        errorMessage = "Date outside chart range"
                    }
                } else if (newValue.length >= 8) {
                    errorMessage = "Invalid date format"
                }
            },
            label = { Text("Enter Date") },
            placeholder = { Text("MM/DD/YYYY") },
            supportingText = {
                when {
                    errorMessage != null -> Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                    )
                    previewWeek != null -> Text("Week of $previewWeek")
                    else -> Text("Enter a date to jump to that week")
                }
            },
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    val parsedDate = parseDateInput(inputText)
                    if (parsedDate != null) {
                        val mondayDate = DateUtils.getMondayOfWeek(parsedDate)
                        if (DateUtils.isDateInRange(mondayDate, chartStartDate)) {
                            onWeekSelected(DateUtils.formatChartDate(mondayDate))
                            inputText = ""
                            previewWeek = null
                            keyboardController?.hide()
                        }
                    }
                },
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Parse date input in various formats:
 * - MM/DD/YYYY
 * - M/D/YYYY
 * - YYYY-MM-DD (ISO format)
 */
private fun parseDateInput(input: String): kotlinx.datetime.LocalDate? {
    if (input.isBlank()) return null

    return try {
        // Try MM/DD/YYYY format
        if (input.contains('/')) {
            val parts = input.split('/')
            if (parts.size == 3) {
                val month = parts[0].toIntOrNull()
                val day = parts[1].toIntOrNull()
                val year = parts[2].toIntOrNull()
                if (month != null && day != null && year != null) {
                    kotlinx.datetime.LocalDate(year, month, day)
                } else {
                    null
                }
            } else {
                null
            }
        } else if (input.contains('-')) {
            // ISO format (YYYY-MM-DD)
            DateUtils.parseChartDate(input)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
