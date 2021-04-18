package com.m14n.billib.data.billboard

import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader
import java.util.*

fun main() {
    val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
            load(stream)
        }
    }
    val today = properties.getProperty("data.today").toChartDate()
    val root = File(properties.getProperty("data.json.root"))
    val theMetadataFile = File(root, "metadata_billboard.json")
    val theMetadata = Json.decodeFromString(BBJournalMetadata.serializer(), theMetadataFile.readText())
    val theCalendar = Calendar.getInstance()

    theMetadata.charts.forEach { theChartMetadata ->
        val theChartFolder = File(root, theChartMetadata.folder)
        theCalendar.time = BB.CHART_DATE_FORMAT.parse("2018-06-23")
        var thePreviousChart: BBChart? = null
        val endDate = theChartMetadata.endDate?.toChartDate() ?: today
        while (theCalendar.time <= endDate) {
            val theDate = BB.CHART_DATE_FORMAT.format(theCalendar.time)
            val theFileName = theChartMetadata.prefix + "-" + theDate + ".json"
            val theFile = File(theChartFolder, theFileName)
            var theChart: BBChart? = null
            if (theFile.exists()) {
                val theReader = FileReader(theFile)
                try {
                    theChart = Json { ignoreUnknownKeys = true }
                        .decodeFromString(BBChart.serializer(), theFile.readText())
                    if (thePreviousChart != null) {
                        if (!checkConsistency(thePreviousChart, theChart)) {
                            println(
                                String.format(
                                    "=========== ERROR ========== %s %s ",
                                    theChartMetadata.name, theDate
                                )
                            )
                        }
                    }
                } finally {
                    theReader.close()
                }
            } else {
                println(String.format("%s DOES NOT EXIST!", theFileName))
            }
            thePreviousChart = theChart
            theCalendar.add(Calendar.DATE, 7)
        }
    }
}

private fun checkConsistency(previousChart: BBChart, chart: BBChart): Boolean {
    var theResult = true
    for ((rank, title, artist, thePositionInfo) in chart.tracks) {
        val theLastWeek = BB.extractLastWeekRank(thePositionInfo?.lastWeek ?: "--")
        if (theLastWeek > 0 && previousChart.tracks.size >= theLastWeek) {
            val (_, title1, artist1) = previousChart.tracks[theLastWeek - 1]
            if (!(title.equals(title1, ignoreCase = true) && artist.equals(artist1, ignoreCase = true))) {
                println(String.format("CHECK %d", rank))
                theResult = false
            }
        }
    }
    return theResult
}
