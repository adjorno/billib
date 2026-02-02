package com.m14n.billib.data.billboard.html

import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import com.m14n.billib.data.billboard.parser.defaultChartListParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.util.*

// Dates that showed FAILURE in consistency checker
val INCONSISTENT_DATES = listOf(
    "2025-05-31",
    "2025-06-07",
    "2025-06-14"
)

private val jsonDecoder = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    ignoreUnknownKeys = true
}

fun main() {
    val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
            load(stream)
        }
    }

    val root = File(properties.getProperty("data.json.root"))
    val theMetadata = Json.decodeFromString<BBJournalMetadata>(File(root, "metadata_billboard.json").readText())

    // Find Hot 100 chart metadata
    val hot100 = theMetadata.charts.find { it.name == "Hot 100" }
    if (hot100 == null) {
        println("ERROR: Hot 100 chart not found in metadata")
        return
    }

    val chartDir = File(root, hot100.folder)
    val refetchDir = File(root, "refetched")
    refetchDir.mkdirs()

    INCONSISTENT_DATES.forEach { date ->
        println("\n" + "=".repeat(80))
        println("Refetching: Hot 100 - $date")
        println("=".repeat(80))

        try {
            // Read old version
            val oldFile = File(chartDir, "${hot100.prefix}-$date.json")
            val oldChart = if (oldFile.exists()) {
                Json.decodeFromString<BBChart>(oldFile.readText())
            } else {
                null
            }

            // Fetch new version from Billboard
            Thread.sleep(3000) // Be nice to Billboard's servers
            val document = BBHtmlParser.getChartDocument(theMetadata, hot100, date)
            val tracksParser = defaultChartListParser()
            val newChart = BBChart(
                name = hot100.name,
                date = date,
                tracks = tracksParser.parse(document)
            )

            // Save refetched version
            val newFile = File(refetchDir, "${hot100.prefix}-$date-refetched.json")
            FileWriter(newFile).use {
                it.write(jsonDecoder.encodeToString(newChart))
            }

            // Compare
            if (oldChart != null) {
                compareCharts(oldChart, newChart, date)
            } else {
                println("✓ Successfully fetched (no old version to compare)")
                println("  Tracks: ${newChart.tracks.size}")
            }

        } catch (e: Exception) {
            println("✗ FAILED to fetch: ${e.message}")
            e.printStackTrace()
        }
    }

    println("\n" + "=".repeat(80))
    println("Refetch complete. New files saved to: ${refetchDir.absolutePath}")
    println("=".repeat(80))
}

fun compareCharts(old: BBChart, new: BBChart, date: String) {
    val differences = mutableListOf<String>()

    // Compare track counts
    if (old.tracks.size != new.tracks.size) {
        differences.add("Track count changed: ${old.tracks.size} → ${new.tracks.size}")
    }

    // Compare positions
    val changedPositions = mutableListOf<String>()
    val maxSize = maxOf(old.tracks.size, new.tracks.size)

    for (i in 0 until maxSize) {
        val oldTrack = old.tracks.getOrNull(i)
        val newTrack = new.tracks.getOrNull(i)

        when {
            oldTrack == null && newTrack != null -> {
                changedPositions.add("  #${i+1}: [NEW] ${newTrack.artist} - ${newTrack.title}")
            }
            oldTrack != null && newTrack == null -> {
                changedPositions.add("  #${i+1}: [REMOVED] ${oldTrack.artist} - ${oldTrack.title}")
            }
            oldTrack != null && newTrack != null -> {
                if (oldTrack.title != newTrack.title || oldTrack.artist != newTrack.artist) {
                    changedPositions.add("  #${i+1}: ${oldTrack.artist} - ${oldTrack.title}")
                    changedPositions.add("      → ${newTrack.artist} - ${newTrack.title}")
                }
                // Check last week rank changes
                val oldLastWeek = oldTrack.positionInfo?.lastWeek
                val newLastWeek = newTrack.positionInfo?.lastWeek
                if (oldLastWeek != newLastWeek) {
                    changedPositions.add("      Last week: $oldLastWeek → $newLastWeek")
                }
            }
        }
    }

    // Print summary
    if (changedPositions.isEmpty()) {
        println("✓ No changes detected - Billboard data is identical")
    } else {
        println("✗ CHANGES DETECTED:")
        differences.forEach { println("  $it") }
        println("\nPosition changes (first 10):")
        changedPositions.take(10).forEach { println(it) }
        if (changedPositions.size > 10) {
            println("  ... and ${changedPositions.size - 10} more changes")
        }
    }
}
