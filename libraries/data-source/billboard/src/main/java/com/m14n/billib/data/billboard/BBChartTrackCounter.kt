package com.m14n.billib.data.billboard

import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBChartMetadata
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import kotlinx.serialization.json.Json

import java.io.File
import java.util.*

fun main() {
    val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
            load(stream)
        }
    }
    val root = File(properties.getProperty("data.json.root"))
    val theMetadataFile = File(root, "metadata_billboard.json")
    val theMetadata = Json.decodeFromString<BBJournalMetadata>(theMetadataFile.readText())

    theMetadata.charts.forEach {
        println(it.name + " " + countBBChartTracks(root, it) + " tracks.")
    }
}

fun countBBChartTracks(root: File, chartMetadata: BBChartMetadata): Int {
    val theChartDir = File(root, chartMetadata.folder)
    var theResult = 0
    for (theChartFile in theChartDir.listFiles()!!) {
        val theChart = Json.decodeFromString<BBChart>(theChartFile.readText())
        theResult += theChart.tracks.size
    }
    return theResult
}
