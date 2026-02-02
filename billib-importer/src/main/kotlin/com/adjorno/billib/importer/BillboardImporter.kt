package com.adjorno.billib.importer

import com.adjorno.billib.importer.db.*
import com.adjorno.billib.importer.model.BBChart
import com.adjorno.billib.importer.model.BBJournalMetadata
import com.adjorno.billib.importer.util.DatabaseConnection
import com.adjorno.billib.importer.util.ProgressTracker
import com.google.gson.Gson
import java.io.File
import java.sql.Connection

class BillboardImporter(private val config: ImportConfig) {

    private val progress = ProgressTracker(config.enableProgressTracking)
    private val gson = Gson()

    fun import() {
        progress.section("Billboard Chart Data Importer")
        progress.log("Data path: ${config.dataPath}")
        progress.log("Database: ${config.dbUrl}")
        progress.log("Batch size: ${config.batchSize}")

        // Test database connection
        progress.log("\nTesting database connection...")
        if (!DatabaseConnection.testConnection(config)) {
            throw RuntimeException("Failed to connect to database. Please check your configuration.")
        }
        progress.log("Database connection successful!")

        // Load metadata
        progress.section("Loading Metadata")
        val metadata = loadMetadata()
        progress.log("Loaded metadata for ${metadata.charts.size} chart types")

        // Scan and parse JSON files
        progress.section("Scanning JSON Files")
        val allCharts = scanChartFiles(metadata)
        progress.log("Found ${allCharts.size} chart files")

        // Extract unique entities
        progress.section("Extracting Unique Entities")
        val uniqueArtists = extractUniqueArtists(allCharts)
        val uniqueTracks = extractUniqueTracks(allCharts)
        val uniqueWeeks = extractUniqueWeeks(allCharts)

        progress.log("Unique artists: ${uniqueArtists.size}")
        progress.log("Unique tracks: ${uniqueTracks.size}")
        progress.log("Unique weeks: ${uniqueWeeks.size}")

        // Import data
        DatabaseConnection.create(config).use { conn ->
            importData(conn, metadata, allCharts, uniqueArtists, uniqueTracks, uniqueWeeks)
        }

        progress.section("Import Complete!")
        progress.log("All data has been successfully imported into PostgreSQL")
    }

    private fun loadMetadata(): BBJournalMetadata {
        val metadataFile = File(config.dataPath, config.metadataFile)
        if (!metadataFile.exists()) {
            throw RuntimeException("Metadata file not found: ${metadataFile.absolutePath}")
        }
        return gson.fromJson(metadataFile.readText(), BBJournalMetadata::class.java)
    }

    private fun scanChartFiles(metadata: BBJournalMetadata): List<BBChart> {
        val allCharts = mutableListOf<BBChart>()

        metadata.charts.forEach { chartMetadata ->
            val chartFolder = File(config.dataPath, chartMetadata.folder)
            if (chartFolder.exists() && chartFolder.isDirectory) {
                val jsonFiles = chartFolder.listFiles { file ->
                    file.extension == "json" && file.name.startsWith(chartMetadata.prefix)
                }

                jsonFiles?.forEach { file ->
                    try {
                        val chart = gson.fromJson(file.readText(), BBChart::class.java)
                        allCharts.add(chart)
                    } catch (e: Exception) {
                        progress.log("WARNING: Failed to parse ${file.name}: ${e.message}")
                    }
                }

                progress.log("  ${chartMetadata.name}: ${jsonFiles?.size ?: 0} files")
            } else {
                progress.log("WARNING: Chart folder not found: ${chartFolder.absolutePath}")
            }
        }

        return allCharts
    }

    private fun extractUniqueArtists(charts: List<BBChart>): Set<String> {
        return charts.flatMap { chart ->
            chart.tracks.map { it.artist.trim() }
        }.toSet()
    }

    private fun extractUniqueTracks(charts: List<BBChart>): Set<TrackKey> {
        return charts.flatMap { chart ->
            chart.tracks.map { TrackKey(it.title.trim(), it.artist.trim()) }
        }.toSet()
    }

    private fun extractUniqueWeeks(charts: List<BBChart>): Set<String> {
        return charts.map { it.date }.toSet()
    }

    private fun importData(
        conn: Connection,
        metadata: BBJournalMetadata,
        allCharts: List<BBChart>,
        uniqueArtists: Set<String>,
        uniqueTracks: Set<TrackKey>,
        uniqueWeeks: Set<String>
    ) {
        // Phase 1: Import dimension tables
        progress.section("Phase 1: Importing Dimension Tables")

        val artistImporter = ArtistImporter(conn, progress, config.batchSize)
        val artistMap = artistImporter.importArtists(uniqueArtists)

        val trackImporter = TrackImporter(conn, progress, config.batchSize)
        val trackMap = trackImporter.importTracks(uniqueTracks, artistMap)

        val chartImporter = ChartImporter(conn, progress)
        val chartMap = chartImporter.importCharts(metadata.charts)
        val weekMap = chartImporter.importWeeks(uniqueWeeks)

        val chartListImporter = ChartListImporter(conn, progress, config.batchSize)
        val chartListMap = chartListImporter.importChartLists(allCharts, chartMap, weekMap)

        // Phase 2: Import fact table
        progress.section("Phase 2: Importing Chart Positions")

        val positionImporter = ChartPositionImporter(conn, progress, config.batchSize)
        positionImporter.importChartPositions(
            allCharts, trackMap, artistMap, chartMap, weekMap, chartListMap
        )

        // Phase 3: Refresh materialized views
        progress.section("Phase 3: Refreshing Materialized Views")
        positionImporter.refreshMaterializedViews()
    }
}
