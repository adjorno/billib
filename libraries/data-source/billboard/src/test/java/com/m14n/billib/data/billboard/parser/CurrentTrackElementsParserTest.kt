package com.m14n.billib.data.billboard.parser

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentTrackElementsParserTest {

    private val sut = CurrentChartListParser()

    @Test
    fun `should parse correctly`() {
        val doc = Jsoup.parse(
            javaClass.classLoader.getResourceAsStream("samples/2021-11-25/country_2021_11_27.html"),
            "UTF-8",
            "test"
        )
        assertEquals(50, sut.parse(doc).toList().size)
    }
}