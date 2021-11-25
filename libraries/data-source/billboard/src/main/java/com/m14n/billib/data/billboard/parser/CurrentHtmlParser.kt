package com.m14n.billib.data.billboard.parser

import DelegateHtmlChartDateParser
import HtmlChartTextDateParser
import com.m14n.billib.data.billboard.BB
import com.m14n.billib.data.billboard.html.jsoup.requestAttr
import com.m14n.billib.data.billboard.model.BBTrack
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator

/**
 * Update of November 2021: The parser is unified across all Billboard charts.
 *
 * As of June 2020 Billboard has different types of html structures for charts.
 * This parser helps to parse date for Hot-100 html type. Samples can be found
 * in the test resource folder.
 */
fun dateParser() =
    DelegateHtmlChartDateParser(
        CurrentTextDateParser(),
        DateFormatParser(BB.CHART_DATE_FORMAT)
    )

class CurrentTextDateParser : HtmlChartTextDateParser {
    override fun parse(document: Document): String = document.body()
        .selectFirst("div#chart-date-picker")
        .requestAttr("data-date")
}

class CurrentChartListParser : HtmlChartListParser {
    private val trackElementParser = CurrentTrackElementParser()

    private val jsonDecoder = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun parse(document: Document): List<BBTrack> {
        val chartListElements = document.body()
            .select(Evaluator.Class("o-chart-results-list-row-container"))
        return chartListElements.map { element ->
            trackElementParser.parse(element)
        }
    }
}
