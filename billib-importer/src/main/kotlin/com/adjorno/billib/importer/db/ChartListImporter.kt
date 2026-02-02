package com.adjorno.billib.importer.db

import com.adjorno.billib.importer.model.BBChart
import com.adjorno.billib.importer.util.ProgressTracker
import java.sql.Connection

data class ChartListKey(val chartId: Long, val weekId: Long)

class ChartListImporter(
    private val connection: Connection,
    private val progress: ProgressTracker,
    private val batchSize: Int = 5000
) {

    fun importChartLists(
        charts: List<BBChart>,
        chartMap: Map<String, Long>,
        weekMap: Map<String, Long>
    ): Map<ChartListKey, Long> {
        progress.log("Importing ${charts.size} chart instances...")

        val chartListMap = mutableMapOf<ChartListKey, Long>()

        // Group charts by (chartId, weekId) to handle potential duplicates
        val uniqueChartLists = charts
            .mapNotNull { chart ->
                val chartId = chartMap[chart.name]
                val weekId = weekMap[chart.date]
                if (chartId != null && weekId != null) {
                    ChartListKey(chartId, weekId)
                } else {
                    null
                }
            }
            .distinct()

        // Insert chart lists
        val insertSql = """
            INSERT INTO CHART_LIST (CHART_ID, WEEK_ID, NUMBER)
            VALUES (?, ?, 1)
            ON CONFLICT (CHART_ID, WEEK_ID) DO NOTHING
        """.trimIndent()

        connection.prepareStatement(insertSql).use { stmt ->
            var count = 0
            uniqueChartLists.forEach { key ->
                stmt.setLong(1, key.chartId)
                stmt.setLong(2, key.weekId)
                stmt.addBatch()
                count++

                if (count % batchSize == 0) {
                    stmt.executeBatch()
                    connection.commit()
                    progress.track("Chart Lists", count, uniqueChartLists.size)
                }
            }

            if (count % batchSize != 0) {
                stmt.executeBatch()
                connection.commit()
            }
            progress.track("Chart Lists", uniqueChartLists.size, uniqueChartLists.size)
        }

        // Read back chart list IDs
        val selectSql = "SELECT _id, CHART_ID, WEEK_ID FROM CHART_LIST"
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery(selectSql)
            while (rs.next()) {
                val key = ChartListKey(rs.getLong("CHART_ID"), rs.getLong("WEEK_ID"))
                chartListMap[key] = rs.getLong("_id")
            }
        }

        progress.log("Imported ${chartListMap.size} chart instances")
        return chartListMap
    }
}
