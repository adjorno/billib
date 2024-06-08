package com.m14n.billib.data.billboard.html

import com.m14n.billib.data.billboard.BB
import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import com.m14n.billib.data.billboard.parser.defaultChartListParser
import com.m14n.ex.BenchmarkCore
import defaultDateParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

object BBHtmlReader {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val properties = Properties().apply {
            ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
                load(stream)
            }
        }

        val root = File(properties.getProperty("data.json.root"))
        val theMetadataFile = File(root, "metadata_billboard.json")
        val theMetadata = Json.decodeFromString<BBJournalMetadata>(theMetadataFile.readText())

        var theWeekFolder: File? = null
        var theWeekDate: Date? = null
        val dateParser = defaultDateParser()
        val tracksParser = defaultChartListParser()
        theMetadata.charts.forEach {
            val theBenchmark = BenchmarkCore.start(it.folder)
            val theDocument = BBHtmlParser.getChartDocument(theMetadata, it, null)
            if (theWeekDate == null) {
                theWeekDate = dateParser.parse(theDocument)
                theWeekFolder = File(root, "week-" + BB.CHART_DATE_FORMAT.format(theWeekDate))
                theWeekFolder?.mkdirs()
            }
            val theChart = BBChart(
                name = it.name,
                date = BB.CHART_DATE_FORMAT.format(theWeekDate),
                tracks = tracksParser.parse(theDocument)
            )
            BenchmarkCore.stop(theBenchmark)
            writeChartToFile(theChart, theWeekFolder!!,
                    it.prefix + "-" + theWeekDate + ".json")
        }
    }

    @Throws(IOException::class)
    fun writeChartToFile(chart: BBChart, folder: File, fileName: String) {
        val theChartFile = File(folder, fileName)
        FileWriter(theChartFile).use {
            it.write(Json.encodeToString(chart))
        }
    }
}
