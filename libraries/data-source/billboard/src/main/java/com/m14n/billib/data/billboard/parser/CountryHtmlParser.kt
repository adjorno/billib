package com.m14n.billib.data.billboard.parser

import DelegateHtmlChartDateParser
import HtmlChartDateParser
import HtmlChartTextDateParser
import com.m14n.billib.data.billboard.BB
import com.m14n.billib.data.billboard.html.jsoup.nodeText
import com.m14n.billib.data.billboard.html.jsoup.requestElementById
import com.m14n.billib.data.billboard.html.jsoup.requestElementsByClass
import com.m14n.billib.data.billboard.html.jsoup.requestElementsByTag
import com.m14n.billib.data.billboard.model.BBPositionInfo
import com.m14n.billib.data.billboard.model.BBTrack
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator

/**
 * As of June 2020 Billboard has different types of html structures for charts.
 * This parser helps to parse date for Country html type. Samples can be found
 * in the test resource folder
 */
fun countryDateParser(): HtmlChartDateParser =
    DelegateHtmlChartDateParser(
        CountryHtmlTextDateParser(),
        DateFormatParser(BB.CHART_DATE_FORMAT)
    )

fun countryChartListParser(): HtmlChartListParser =
    DelegateChartListParser(
        CountryTrackElementsParser(),
        CountryTrackParser()
    )

class CountryHtmlTextDateParser : HtmlChartTextDateParser {
    override fun parse(document: Document): String = document
        .select(Evaluator.Id("chart-date-picker")).attr("data-date")
}

class CountryTrackElementsParser : HtmlTrackElementsParser {
    override fun parse(document: Document): Elements = document
        .select(Evaluator.Class("o-chart-results-list-row"))
}

class CountryTrackParser : TrackElementParser {
    override fun parse(element: Element): BBTrack {
        val itemDetails = element.requestElementsByClass("item-details").first()
        val title = itemDetails
            .requestElementsByClass("item-details__title")
            .first()
            .textNodes().firstOrNull()?.text()?.trim() ?: "No title"
        val artist = itemDetails
            .requestElementsByClass("item-details__artist")
            .first()
            .nodeText()
        val positionDetails = element.requestElementsByTag("td")
        val lastWeek = positionDetails[1].nodeText()
        val rank = positionDetails[2].nodeText().toInt()
        val peekPosition = try {
            positionDetails[0].nodeText().toInt()
        } catch (e: NumberFormatException) {
            rank
        }
        val wksOnChart = positionDetails[6].nodeText().toInt()
        return BBTrack(rank, title, artist, BBPositionInfo(lastWeek, peekPosition, wksOnChart))
    }
}
