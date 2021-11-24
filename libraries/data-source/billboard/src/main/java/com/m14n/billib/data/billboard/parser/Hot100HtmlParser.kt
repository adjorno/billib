package com.m14n.billib.data.billboard.parser

import DelegateHtmlChartDateParser
import HtmlChartTextDateParser
import com.m14n.billib.data.billboard.BB
import com.m14n.billib.data.billboard.html.jsoup.requestAttr
import com.m14n.billib.data.billboard.html.jsoup.requestElementById
import com.m14n.billib.data.billboard.model.BBPositionInfo
import com.m14n.billib.data.billboard.model.BBTrack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator

/**
 * As of June 2020 Billboard has different types of html structures for charts.
 * This parser helps to parse date for Hot-100 html type. Samples can be found
 * in the test resource folder.
 */
fun hot100DateParser() =
    DelegateHtmlChartDateParser(
        Hot100TextDateParser(),
        DateFormatParser(BB.CHART_DATE_FORMAT)
    )

class Hot100TextDateParser : HtmlChartTextDateParser {
    override fun parse(document: Document): String = document.body()
        .selectFirst("div#chart-date-picker")
        .requestAttr("data-date")
}

class Hot100ChartListParser : HtmlChartListParser {
    private val jsonDecoder = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun parse(document: Document): List<BBTrack> {
        val chartListElements = document.body()
            .select(Evaluator.Class("o-chart-results-list-row-container"))
        return chartListElements.map { element ->
            val rankElement = element.selectFirst(Evaluator.Class("o-chart-results-list__item")).selectFirst(Evaluator.Class("c-label"))
            val titleElement = element.selectFirst(Evaluator.Id("title-of-a-story"))
            val artistElement = titleElement.parent().selectFirst(Evaluator.Class("c-label"))
            val chartTrackDetails = titleElement.parent().parent().select(Evaluator.Class("o-chart-results-list__item"))
            val lastWeekElement = chartTrackDetails[3].selectFirst(Evaluator.Class("c-label"))
            val peakPositionElement = chartTrackDetails[4].selectFirst(Evaluator.Class("c-label"))
            val wksOnChartElement = chartTrackDetails[5].selectFirst(Evaluator.Class("c-label"))
            BBTrack(
                title = titleElement.text().trim(),
                artist = artistElement.text().trim(),
                rank = rankElement.text().trim().toInt(),
                positionInfo = BBPositionInfo(
                    lastWeek = lastWeekElement.text().trim(),
                    peekPosition = peakPositionElement.text().trim().toInt(),
                    wksOnChart = wksOnChartElement.text().trim().toInt()
                )
            )
        }
    }
}

@Serializable
data class Hot100Track(
    @SerialName("artist_name") var artist: String,
    @SerialName("history") var history: Hot100History,
    @SerialName("title") var title: String,
    @SerialName("rank") var rank: String
)

@Serializable
data class Hot100History(
    @SerialName("peak_rank") var peakRank: String,
    @SerialName("last_week") var lastWeak: String?,
    @SerialName("weeks_on_chart") var weeksOnChart: String
)
