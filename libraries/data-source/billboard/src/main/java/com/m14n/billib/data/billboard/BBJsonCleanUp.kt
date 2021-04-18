package com.m14n.billib.data.billboard

import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.util.*

fun main() {
    val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
            load(stream)
        }
    }
    val root = File(properties.getProperty("data.json.root"))
    val today = properties.getProperty("data.today")

    val originalMetadata = Json.decodeFromString<BBJournalMetadata>(
        File(root, "metadata_billboard.json").readText()
    )
    originalMetadata.charts.forEach { chartMetadata ->
        generateBillboardDateSequence(
            startDate = chartMetadata.startDate.date,
            endDate = (chartMetadata.endDate ?: today).date
        ).forEach { weekDate ->
            val originalChartListFile = File(
                File(root, chartMetadata.folder),
                "${chartMetadata.prefix}-${weekDate.text}.json"
            )
            if (originalChartListFile.exists()) {
                val originalChartList = Json { ignoreUnknownKeys = true }
                    .decodeFromString<BBChart>(originalChartListFile.readText())
                println("Chart list $originalChartList has found")
                FileWriter(originalChartListFile).use {
                    it.write(Json {
                        prettyPrint = true
                        prettyPrintIndent = "  "
                    }.encodeToString(originalChartList))
                }
            }
        }
    }
}
