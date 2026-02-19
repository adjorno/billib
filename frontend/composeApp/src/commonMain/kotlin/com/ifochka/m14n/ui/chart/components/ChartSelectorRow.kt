package com.ifochka.m14n.ui.chart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ifochka.m14n.data.model.Chart

@Composable
fun ChartSelectorRow(
    availableCharts: List<Chart>,
    selectedChart: Chart,
    onChartSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(availableCharts, key = { it.id ?: 0 }) { chart ->
            FilterChip(
                selected = chart.id == selectedChart.id,
                onClick = { chart.id?.let { onChartSelected(it) } },
                label = { Text(text = chart.name ?: "Unknown") },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}
