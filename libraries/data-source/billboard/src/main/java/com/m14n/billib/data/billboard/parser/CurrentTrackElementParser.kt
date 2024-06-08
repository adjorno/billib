package com.m14n.billib.data.billboard.parser

import com.m14n.billib.data.billboard.model.BBPositionInfo
import com.m14n.billib.data.billboard.model.BBTrack
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator

class CurrentTrackElementParser: TrackElementParser {
    override fun parse(element: Element): BBTrack {
        val rankElement = element.selectFirst(Evaluator.Class("o-chart-results-list__item")).selectFirst(Evaluator.Class("c-label"))
        val titleElement = element.selectFirst(Evaluator.Id("title-of-a-story"))
        val artistElement = titleElement.parent().selectFirst(Evaluator.Class("c-label"))
        val chartTrackDetails = titleElement.parent().parent().select(Evaluator.Class("o-chart-results-list__item"))
        val lastWeekElement = chartTrackDetails[3].selectFirst(Evaluator.Class("c-label"))
        val peakPositionElement = chartTrackDetails[4].selectFirst(Evaluator.Class("c-label"))
        val wksOnChartElement = chartTrackDetails[5].selectFirst(Evaluator.Class("c-label"))
        return BBTrack(
            title = titleElement.text().trim(),
            artist = artistElement.text().trim(),
            rank = rankElement.text().trim().toInt(),
            positionInfo = BBPositionInfo(
                lastWeek = lastWeekElement.text().trim(),
                peekPosition = peakPositionElement.text().trim().toInt(),
                wksOnChart = try {
                    wksOnChartElement.text().trim().toInt()
                } catch (e: Exception) {
                    0
                }
            )
        )
    }
}
