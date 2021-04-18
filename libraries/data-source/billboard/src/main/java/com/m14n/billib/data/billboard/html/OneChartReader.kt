package com.m14n.billib.data.billboard.html

import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import com.m14n.billib.data.billboard.parser.Hot100ChartListParser
import com.m14n.billib.data.billboard.parser.defaultChartListParser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.util.*

var DATE = "2018-04-28"
var CHART = "Electronic"

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
        val tracksParser = if (chartMeta.name == "Hot 100") {
            Hot100ChartListParser()
        } else {
            defaultChartListParser()
        }
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
            it.write(Json {
                prettyPrint = true
                prettyPrintIndent = "  "
            }.encodeToString(theChart))
        }
        println("OLOLO ${chartMeta.name} FINISHED")
    }
}
