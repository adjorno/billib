package com.m14n.billib.data.billboard.parser

import com.m14n.billib.data.billboard.model.BBPositionInfo
import com.m14n.billib.data.billboard.model.BBTrack
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

/**
 * February 2026: Billboard changed chart stats structure from positional list items
 * to labeled flex divs. Old format (pre-2026) is no longer supported.
 */
class CurrentTrackElementParser : TrackElementParser {
    override fun parse(element: Element): BBTrack {
        val rankElement = element.selectFirst(
            Evaluator.Class("o-chart-results-list__item"),
        )!!.selectFirst(Evaluator.Class("c-label"))!!
        val titleElement = element.selectFirst(Evaluator.Id("title-of-a-story"))!!
        val artistElement = titleElement.parent()!!.selectFirst(Evaluator.Class("c-label"))!!

        val statsContainer = element.select("span.c-span").find { it.text().trim() == "LW" }?.parent()?.parent()
        val lastWeekElement = statsContainer?.select("span.c-span")?.find { it.text().trim() == "LW" }
            ?.parent()?.selectFirst(Evaluator.Class("c-label"))
        val peakPositionElement = statsContainer?.select("span.c-span")?.find { it.text().trim() == "PEAK" }
            ?.parent()?.selectFirst(Evaluator.Class("c-label"))
        val wksOnChartElement = statsContainer?.select("span.c-span")?.find { it.text().trim() == "WEEKS" }
            ?.parent()?.selectFirst(Evaluator.Class("c-label"))

        return BBTrack(
            title = titleElement.text().trim(),
            artist = artistElement.text().trim(),
            rank = rankElement.text().trim().toInt(),
            positionInfo = BBPositionInfo(
                lastWeek = lastWeekElement?.text()?.trim() ?: "-",
                peekPosition = peakPositionElement?.text()?.trim()?.toIntOrNull() ?: 0,
                wksOnChart = try {
                    wksOnChartElement?.text()?.trim()?.toInt() ?: 0
                } catch (e: Exception) {
                    0
                },
            ),
        )
    }
}
