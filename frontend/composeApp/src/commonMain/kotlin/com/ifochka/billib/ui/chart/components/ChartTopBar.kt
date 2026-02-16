package com.ifochka.billib.ui.chart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ifochka.billib.data.model.Chart

@Composable
fun ChartTopBar(
    weekDate: String,
    availableCharts: List<Chart>,
    selectedChart: Chart,
    onChartSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Week picker
        WeekPicker(
            weekDate = weekDate,
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
}
