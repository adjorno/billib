package com.adjorno.billib.importer.db

import com.adjorno.billib.importer.model.BBChart
import com.adjorno.billib.importer.util.ProgressTracker
import java.sql.Connection
import java.sql.Date

class ChartPositionImporter(
    private val connection: Connection,
    private val progress: ProgressTracker,
    private val batchSize: Int = 5000
) {

    fun importChartPositions(
        charts: List<BBChart>,
        trackMap: Map<TrackKey, Long>,
        artistMap: Map<String, Long>,
        chartMap: Map<String, Long>,
        weekMap: Map<String, Long>,
        chartListMap: Map<ChartListKey, Long>
    ) {
        progress.log("Importing chart positions for ${charts.size} charts...")

        val insertSql = """
            INSERT INTO CHART_TRACK_POSITION (
                TRACK_ID, CHART_LIST_ID, _RANK, LAST_WEEK_RANK,
                WEEK_DATE, CHART_ID, ARTIST_ID, TRACK_TITLE, ARTIST_NAME
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING
        """.trimIndent()

        // Also insert into legacy CHART_TRACK table
        val legacyInsertSql = """
            INSERT INTO CHART_TRACK (TRACK_ID, CHART_LIST_ID, _RANK, LAST_WEEK_RANK)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (TRACK_ID, CHART_LIST_ID) DO NOTHING
        """.trimIndent()

        var totalPositions = 0
        var processedCharts = 0

        connection.prepareStatement(insertSql).use { stmt ->
            connection.prepareStatement(legacyInsertSql).use { legacyStmt ->

                charts.forEach { chart ->
                    val chartId = chartMap[chart.name]
                    val weekId = weekMap[chart.date]
                    val weekDate = parseDate(chart.date)

                    if (chartId != null && weekId != null && weekDate != null) {
                        val chartListKey = ChartListKey(chartId, weekId)
                        val chartListId = chartListMap[chartListKey]

                        if (chartListId != null) {
                            chart.tracks.forEach { track ->
                                val trackKey = TrackKey(track.title.trim(), track.artist.trim())
                                val trackId = trackMap[trackKey]
                                val artistId = artistMap[track.artist.trim()]

                                if (trackId != null && artistId != null) {
                                    val lastWeekRank = parseLastWeekRank(track.position?.lastWeek)

                                    // Insert into CHART_TRACK_POSITION
                                    stmt.setLong(1, trackId)
                                    stmt.setLong(2, chartListId)
                                    stmt.setInt(3, track.rank)
                                    stmt.setInt(4, lastWeekRank)
                                    stmt.setDate(5, weekDate)
                                    stmt.setLong(6, chartId)
                                    stmt.setLong(7, artistId)
                                    stmt.setString(8, track.title.trim())
                                    stmt.setString(9, track.artist.trim())
                                    stmt.addBatch()

                                    // Insert into legacy CHART_TRACK
                                    legacyStmt.setLong(1, trackId)
                                    legacyStmt.setLong(2, chartListId)
                                    legacyStmt.setInt(3, track.rank)
                                    legacyStmt.setInt(4, lastWeekRank)
                                    legacyStmt.addBatch()

                                    totalPositions++

                                    if (totalPositions % batchSize == 0) {
                                        stmt.executeBatch()
                                        legacyStmt.executeBatch()
                                        connection.commit()
                                        progress.track("Chart Positions", totalPositions, charts.sumOf { it.tracks.size })
                                    }
                                }
                            }
                        }
                    }

                    processedCharts++
                }

                // Execute remaining batch
                if (totalPositions % batchSize != 0) {
                    stmt.executeBatch()
                    legacyStmt.executeBatch()
                    connection.commit()
                }
            }
        }

        progress.track("Chart Positions", totalPositions, totalPositions)
        progress.log("Imported $totalPositions chart positions from $processedCharts charts")
    }

    private fun parseDate(dateStr: String): Date? {
        return try {
            // Expected format: yyyy-MM-dd
            Date.valueOf(dateStr)
        } catch (e: Exception) {
            progress.log("WARNING: Invalid date format: $dateStr")
            null
        }
    }

    private fun parseLastWeekRank(lastWeekStr: String?): Int {
        return when {
            lastWeekStr == null -> 0
            lastWeekStr == "--" -> 0
            lastWeekStr.startsWith("*") -> 0
            lastWeekStr.startsWith("RE") -> 0
            lastWeekStr.startsWith("NEW") -> 0
            else -> lastWeekStr.trim().toIntOrNull() ?: 0
        }
    }

    fun refreshMaterializedViews() {
        progress.log("Refreshing materialized views...")

        connection.createStatement().use { stmt ->
            stmt.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY GLOBAL_RANK_TRACK")
            progress.log("Refreshed GLOBAL_RANK_TRACK")

            stmt.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY GLOBAL_RANK_ARTIST")
            progress.log("Refreshed GLOBAL_RANK_ARTIST")
        }

        connection.commit()
        progress.log("Materialized views refreshed successfully")
    }
}
