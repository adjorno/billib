package com.m14n.billib.data.billboard.parser

import com.m14n.billib.data.billboard.html.jsoup.requestElementById
import com.m14n.billib.data.billboard.html.jsoup.requestElementsByClass
import com.m14n.billib.data.billboard.html.jsoup.requestElementsByTag
import com.m14n.billib.data.billboard.model.BBPositionInfo
import com.m14n.billib.data.billboard.model.BBTrack
import org.jsoup.Jsoup
import org.jsoup.select.Evaluator
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentTrackParserTest {

    private val sut = CurrentTrackElementParser()

    @Test
    fun `should parse correctly`() {
        val document = Jsoup.parse(
            javaClass.classLoader.getResourceAsStream("samples/2021-11-25/country_2021_11_27.html"),
            "UTF-8",
            "test"
        )
        val trackElement = document
            .selectFirst(Evaluator.Class("o-chart-results-list-row-container"))
        val expectedTrack = BBTrack(
            rank = 1,
            title = "All Too Well (Taylor's Version)",
            artist = "Taylor Swift",
            positionInfo = BBPositionInfo(
                lastWeek = "-",
                peekPosition = 1,
                wksOnChart = 1
            )
        )
        assertEquals(expectedTrack, sut.parse(trackElement))
    }
}