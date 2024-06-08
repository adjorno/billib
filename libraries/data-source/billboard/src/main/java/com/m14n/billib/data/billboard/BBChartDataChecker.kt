package com.m14n.billib.data.billboard

import com.m14n.billib.data.billboard.model.consistency.legacyChartConsistencyChecker
import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBChartMetadata
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

private val json: Json by lazy {
    Json { ignoreUnknownKeys = true }
}

fun main() {
    val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
            load(stream)
        }
    }
    val today = properties.getProperty("data.today").toChartDate()
    val root = File(properties.getProperty("data.json.root"))
    val theMetadataFile = File(root, "metadata_billboard.json")
    val theMetadata = Json.decodeFromString<BBJournalMetadata>(theMetadataFile.readText())

    checkTheWholeChart(theMetadata.charts[0], root, today, "1965-01-02")
//    theMetadata.charts.forEach { theChartMetadata ->
//        checkChart(theChartMetadata, root, theCalendar, today)
//    }
}

private fun checkTheWholeChart(
    theChartMetadata: BBChartMetadata,
    root: File,
    today: Date,
    checkUntilDate: String? = theChartMetadata.endDate,
) {
    val theChartFolder = File(root, theChartMetadata.folder)
    var thePreviousChart: BBChart? = null
    generateBillboardDateSequence(
        startDate = BB.CHART_DATE_FORMAT.parse(theChartMetadata.startDate),
        endDate = checkUntilDate?.toChartDate() ?: today,
    ).forEach { weekDate ->
        val theDate = BB.CHART_DATE_FORMAT.format(weekDate)
        val theFileName = theChartMetadata.prefix + "-" + theDate + ".json"
        val theFile = File(theChartFolder, theFileName)
        var theChart: BBChart? = null
        if (theFile.exists()) {
            theChart = json.decodeFromString<BBChart>(theFile.readText())
            if (thePreviousChart != null) {
                val chartConsistencyResult = legacyChartConsistencyChecker.check(
                    previousChart = thePreviousChart!!, chart = theChart
                )
                if (chartConsistencyResult.inconsistencies.isNotEmpty()) {
                    println("${theChart.name} - ${theChart.date} : $chartConsistencyResult")
                }
            }
        } else {
            println(String.format("%s DOES NOT EXIST!", theFileName))
        }
        thePreviousChart = theChart
    }
}
