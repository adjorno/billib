package com.m14n.billib.data.billboard.parser

import LogOnErrorChartDateParser
import defaultDateParser
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.logging.Logger

class HtmlChartDateParserTest {

    @Test
    fun `for non-null logger default parser is LogOnErrorChartDateParser`() {
        assertTrue(defaultDateParser(Logger.getGlobal()) is LogOnErrorChartDateParser)
    }
}
