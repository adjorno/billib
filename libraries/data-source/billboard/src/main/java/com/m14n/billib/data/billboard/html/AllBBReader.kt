package com.m14n.billib.data.billboard.html

import com.m14n.billib.data.billboard.BB
import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBChartMetadata
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import com.m14n.billib.data.billboard.model.BBTrack
import com.m14n.billib.data.billboard.parser.CurrentChartListParser
import com.m14n.billib.data.billboard.parser.dateParser
import com.m14n.billib.data.billboard.toChartDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.ParseException
import java.util.*

private val jsonDecoder = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

private var ignoreSkippedWeeks: Int = 5

fun main() {
    val properties = Properties().apply {
        ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
            load(stream)
        }
    }

    properties.getProperty("ignoreSkippedWeeksInRow")?.toInt()?.let { property ->
        ignoreSkippedWeeks = property
    }
    val today = properties.getProperty("data.today").toChartDate()
    val root = File(properties.getProperty("data.json.root"))
    val theMetadataFile = File(root, "metadata_billboard.json")
    val theMetadata = Json.decodeFromString<BBJournalMetadata>(theMetadataFile.readText())

    theMetadata.charts
        .filter { it.endDate == null }
        .forEach {
            fetchChart(root, theMetadata, it, today)
        }
}

@Throws(ParseException::class)
private fun fetchChart(root: File, metadata: BBJournalMetadata, theChartMetadata: BBChartMetadata, date: Date) {
    println("---------------------" + theChartMetadata.name + "----------------------------")
    val theChartDir = File(root, theChartMetadata.folder)
    if (!theChartDir.exists()) {
        theChartDir.mkdirs()
    }
    var theSkip = 0
    val theCalendar = Calendar.getInstance().apply {
        time = date
        add(Calendar.DATE, Calendar.DAY_OF_WEEK)
    }
    val dateParser = dateParser()
    val tracksParser = CurrentChartListParser()

    while (theSkip <= ignoreSkippedWeeks) {
        val theCurrent = BB.CHART_DATE_FORMAT.format(theCalendar.time)
        theCalendar.add(
            Calendar.DATE,
            if ("2018-01-06" == theCurrent) -3 else if ("2018-01-03" == theCurrent) -4 else
                if ("1962-01-06" == theCurrent) -12 else -7
        )
        if (theCalendar.time.before(theChartMetadata.startDate.toChartDate())) {
            break
        }
        val theFormatDate = BB.CHART_DATE_FORMAT.format(theCalendar.time)
        val theChartFile = File(
            theChartDir,
            theChartMetadata.prefix + "-" + theFormatDate + ".json"
        )
        if (!theChartFile.exists()) {
            Thread.sleep(6000)
            try {
                val theChartDocument = BBHtmlParser.getChartDocument(
                    metadata, theChartMetadata,
                    theFormatDate
                )
                val theHtmlDate = try {
                    BB.CHART_DATE_FORMAT.format(dateParser.parse(theChartDocument))
                } catch (e: Exception) {
                    e.printStackTrace()
                    "COULD_NOT_FETCH_DATE"
                }
                if (theFormatDate != theHtmlDate
                    // Billboard mistake as always :-)
                    && !("2018-11-10" == theHtmlDate && "2018-11-03" == theFormatDate &&
                            "Youtube" == theChartMetadata.name)
                ) {
                    println("${theChartMetadata.name} $theFormatDate WRONG DATE!")
                    theSkip++
                    continue
                }
                var theTracks: List<BBTrack>?
                try {
                    theTracks = tracksParser.parse(theChartDocument)
                } catch (e: Exception) {
                    e.printStackTrace()
                    theSkip++
                    continue
                }

                if (theTracks.size != theChartMetadata.size) {
                    print("SIZE = " + theTracks.size + " EXPECTED = " + theChartMetadata.size + " ")
                }
                val theChart = BBChart(
                    name = theChartMetadata.name,
                    date = theFormatDate, tracks = theTracks
                )

                FileWriter(theChartFile).use {
                    it.write(jsonDecoder.encodeToString(theChart))
                }
                println("${theChartMetadata.name} $theFormatDate SUCCESS!")
                theSkip = 0
            } catch (e: IOException) {
                e.printStackTrace()
                theSkip++
            }

        } else {
            theSkip = 0
        }
    }
}

