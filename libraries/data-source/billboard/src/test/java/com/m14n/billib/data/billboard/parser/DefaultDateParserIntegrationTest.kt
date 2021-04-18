package com.m14n.billib.data.billboard.parser

import HtmlChartDateParser
import com.m14n.billib.data.billboard.html.BBHtmlParser
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import defaultDateParser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Logger

class DefaultDateParserIntegrationTest {

    private lateinit var sut: HtmlChartDateParser
    private lateinit var root: File

    @Before
    fun setUp() {
        sut = defaultDateParser(Logger.getLogger("DefaultDateParserIntegrationTest").apply {
            addHandler(ConsoleHandler())
        })
        val properties = Properties().apply {
            ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
                load(stream)
            }
        }
        root = File(properties.getProperty("data.json.root"))
    }

    @Test
    fun `default parser should parse all latest charts except hot 100`() {
        val journal =
            Json.decodeFromString<BBJournalMetadata>(File(root, "metadata_billboard.json").readText())
        journal.charts.asSequence().filter { it.name != "Hot 100" && it.endDate == null }.forEach { chart ->
            if (chart.name != "Hot 100") {
                val doc = BBHtmlParser.getChartDocument(journal, chart)
                sut.parse(doc)
            }
        }
    }
}
