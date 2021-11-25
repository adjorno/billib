package com.m14n.billib.data.billboard.parser

import com.m14n.billib.data.billboard.html.BBHtmlParser
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.*

class CurrentDateParserIntegrationTest {

    private val sut = dateParser()
    private lateinit var root: File

    @Before
    fun setUp() {
        val properties = Properties().apply {
            ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
                load(stream)
            }
        }
        root = File(properties.getProperty("data.json.root"))
    }

    @Test
    fun `should parse the date correctly`() {
        val theMetadata =
            Json.decodeFromString<BBJournalMetadata>(File(root, "metadata_billboard.json").readText())
        theMetadata.charts.first().let { chartMeta ->
            val doc = BBHtmlParser.getChartDocument(theMetadata, chartMeta)
            sut.parse(doc)
        }
    }
}
