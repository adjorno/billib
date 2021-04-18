package com.m14n.billib.data.billboard.parser

import com.m14n.billib.data.billboard.model.BBTrack
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class Hot100ChartListParserTest {

    private val sut = Hot100ChartListParser()

    @Test
    fun `should parse correctly`() {
        val document = Jsoup.parse(
            javaClass.classLoader.getResourceAsStream("samples/2020-06-13/hot_100.html"),
            "UTF-8",
            "test"
        )
        val expectedTracks =
            javaClass.classLoader.getResourceAsStream("samples/2020-06-13/hot_100_tracks.json")?.bufferedReader()?.use {
                Json.decodeFromString<List<BBTrack>>(it.readText())
            }
        assertEquals(expectedTracks, sut.parse(document))
    }
}