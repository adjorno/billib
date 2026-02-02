package com.adjorno.billib.importer.db

import com.adjorno.billib.importer.model.BBChartMetadata
import com.adjorno.billib.importer.util.ProgressTracker
import java.sql.Connection

class ChartImporter(
    private val connection: Connection,
    private val progress: ProgressTracker
) {

    fun importCharts(charts: List<BBChartMetadata>): Map<String, Long> {
        progress.log("Importing ${charts.size} chart types...")

        val chartMap = mutableMapOf<String, Long>()

        // Insert Billboard journal if not exists (ID=1)
        val journalSql = "INSERT INTO JOURNAL (_id, NAME) VALUES (1, 'Billboard') ON CONFLICT (_id) DO NOTHING"
        connection.createStatement().execute(journalSql)

        // Insert charts
        val insertSql = """
            INSERT INTO CHART (NAME, JOURNAL_ID, LIST_SIZE, START_DATE, END_DATE)
            VALUES (?, 1, ?, ?, ?)
            ON CONFLICT DO NOTHING
        """.trimIndent()

        connection.prepareStatement(insertSql).use { stmt ->
            charts.forEach { chart ->
                stmt.setString(1, chart.name)
                stmt.setInt(2, chart.size)
                stmt.setDate(3, java.sql.Date.valueOf(chart.startDate))
                stmt.setDate(4, chart.endDate?.let { java.sql.Date.valueOf(it) })
                stmt.addBatch()
            }
            stmt.executeBatch()
            connection.commit()
        }

        // Read back chart IDs
        val selectSql = "SELECT _id, NAME FROM CHART"
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery(selectSql)
            while (rs.next()) {
                chartMap[rs.getString("NAME")] = rs.getLong("_id")
            }
        }

        progress.log("Imported ${chartMap.size} charts")
        return chartMap
    }

    fun importWeeks(weeks: Set<String>): Map<String, Long> {
        progress.log("Importing ${weeks.size} unique weeks...")

        val weekMap = mutableMapOf<String, Long>()

        // Insert weeks
        val insertSql = "INSERT INTO WEEK (DATE) VALUES (?) ON CONFLICT (DATE) DO NOTHING"
        connection.prepareStatement(insertSql).use { stmt ->
            weeks.forEach { week ->
                stmt.setString(1, week)
                stmt.addBatch()
            }
            stmt.executeBatch()
            connection.commit()
        }

        // Read back week IDs
        val selectSql = "SELECT WEEK_ID, DATE FROM WEEK"
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery(selectSql)
            while (rs.next()) {
                weekMap[rs.getString("DATE")] = rs.getLong("WEEK_ID")
            }
        }

        progress.log("Imported ${weekMap.size} weeks")
        return weekMap
    }
}
