package com.ifochka.m14n.ui.bestsongs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val MAX_YEARS = 99
private const val MAX_MONTHS = 11
private const val MAX_DAYS = 99

@Composable
fun DurationPickerDialog(
    years: Int,
    months: Int,
    days: Int,
    onConfirm: (years: Int, months: Int, days: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var y by remember { mutableIntStateOf(years) }
    var m by remember { mutableIntStateOf(months) }
    var d by remember { mutableIntStateOf(days) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duration") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NumberColumn(
                    label = "Years",
                    value = y,
                    max = MAX_YEARS,
                    onValueChange = { y = it },
                )
                NumberColumn(
                    label = "Months",
                    value = m,
                    max = MAX_MONTHS,
                    onValueChange = { m = it },
                )
                NumberColumn(
                    label = "Days",
                    value = d,
                    max = MAX_DAYS,
                    onValueChange = { d = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(y, m, d) },
                enabled = y > 0 || m > 0 || d > 0,
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
internal fun NumberColumn(
    label: String,
    value: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        IconButton(onClick = { onValueChange((value + 1).coerceAtMost(max)) }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Increase $label")
        }
        Text(text = value.toString(), style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = { onValueChange((value - 1).coerceAtLeast(0)) }) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = "Decrease $label")
        }
    }
}
