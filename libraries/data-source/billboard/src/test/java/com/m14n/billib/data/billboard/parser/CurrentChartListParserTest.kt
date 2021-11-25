package com.m14n.billib.data.billboard.parser

import com.m14n.billib.data.billboard.model.BBTrack
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentChartListParserTest {

    private val sut = CurrentChartListParser()

    @Test
    fun `should parse correctly`() {
        val document = Jsoup.parse(
            javaClass.classLoader.getResourceAsStream("samples/2021-11-25/country_2021_11_27.html"),
            "UTF-8",
            "test"
        )
        val expectedTracks =
            javaClass.classLoader.getResourceAsStream("samples/2021-11-25/country_tracks.json")?.bufferedReader()?.use {
                Json.decodeFromString<List<BBTrack>>(it.readText())
            }
        assertEquals(expectedTracks, sut.parse(document))
    }
}