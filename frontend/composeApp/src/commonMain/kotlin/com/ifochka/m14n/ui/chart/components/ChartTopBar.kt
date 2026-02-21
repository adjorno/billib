package com.ifochka.m14n.ui.chart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.ui.chart.WeekDirection

@Composable
fun ChartTopBar(
    weekDate: String,
    availableCharts: List<Chart>,
    selectedChart: Chart,
    onChartSelected: (Long) -> Unit,
    onWeekNavigate: (WeekDirection) -> Unit,
    onWeekSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDateInput by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Week picker
        WeekPicker(
            weekDate = weekDate,
            chartStartDate = selectedChart.startDate,
            chartEndDate = selectedChart.endDate,
            onOpenDateInput = { showDateInput = true },
            onNavigate = onWeekNavigate,
            modifier = Modifier.padding(16.dp),
        )

        // Chart selector row
        ChartSelectorRow(
            availableCharts = availableCharts,
            selectedChart = selectedChart,
            onChartSelected = onChartSelected,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    // Date picker dialog (shown when user clicks WeekPicker)
    if (showDateInput) {
        WeekDateInput(
            currentWeek = weekDate,
            chartStartDate = selectedChart.startDate,
            onWeekSelected = { selectedDate ->
                onWeekSelect(selectedDate)
            },
            onDismiss = { showDateInput = false },
        )
    }
}
