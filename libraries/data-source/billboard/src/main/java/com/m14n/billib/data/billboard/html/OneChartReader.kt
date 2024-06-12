package com.m14n.billib.data.billboard.html

import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import com.m14n.billib.data.billboard.parser.defaultChartListParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.util.*

var DATE = "1998-10-17"
var CHART = "Latin"

private val jsonDecoder = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

fun main() {
    val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
            load(stream)
        }
    }

    val root = File(properties.getProperty("data.json.root"))
    val theMetadata = Json.decodeFromString<BBJournalMetadata>(File(root, "metadata_billboard.json").readText())

    theMetadata.charts.filter { it.name == CHART }.forEach { chartMeta ->
        println("OLOLO ${chartMeta.name} STARTED")

        val document = BBHtmlParser.getChartDocument(theMetadata, chartMeta, DATE)
        val tracksParser = defaultChartListParser()
        val theChart = BBChart(
            name = chartMeta.name,
            date = DATE,
            tracks = tracksParser.parse(document)
        )
        val theChartDir = File(root, chartMeta.folder)
        val theChartFile = File(
            theChartDir,
            chartMeta.prefix + "-" + DATE + ".json"
        )
        FileWriter(theChartFile).use {
            it.write(jsonDecoder.encodeToString(theChart))
        }
        println("OLOLO ${chartMeta.name} FINISHED")
    }
}
