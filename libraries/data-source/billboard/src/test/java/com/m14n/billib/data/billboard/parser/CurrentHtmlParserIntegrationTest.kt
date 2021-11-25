package com.m14n.billib.data.billboard.parser

import defaultDateParser
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class CurrentHtmlParserIntegrationTest {
    private val sut = defaultDateParser()

    @Test
    fun `should parse the date correctly`() {
        val expectedDate = Calendar.getInstance().apply {
            // 2021 November, 27
            set(2021, Calendar.NOVEMBER, 27)
        }
        val chartFileName = "country_${expectedDate.get(Calendar.YEAR)}_${expectedDate.get(Calendar.MONTH) + 1}_${expectedDate.get(Calendar.DAY_OF_MONTH)}.html"
        val doc = Jsoup.parse(
            javaClass.classLoader.getResourceAsStream(
                "samples/2021-11-25/${chartFileName}"
            ),
            "UTF-8",
            "test"
        )
        val date = sut.parse(doc)
        val actualCalendar = Calendar.getInstance().apply {
            time = date
        }

        assertEquals(expectedDate.get(Calendar.YEAR), actualCalendar.get(Calendar.YEAR))
        assertEquals(expectedDate.get(Calendar.MONTH), actualCalendar.get(Calendar.MONTH))
        assertEquals(expectedDate.get(Calendar.DAY_OF_MONTH), actualCalendar.get(Calendar.DAY_OF_MONTH))
    }
}
