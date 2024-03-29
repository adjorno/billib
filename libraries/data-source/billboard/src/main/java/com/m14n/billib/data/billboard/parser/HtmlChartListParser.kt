package com.m14n.billib.data.billboard.parser

import com.m14n.billib.data.billboard.model.BBTrack
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.text.ParseException

fun defaultChartListParser() = CurrentChartListParser()

interface HtmlChartListParser {
    @Throws(ParseException::class)
    fun parse(document: Document): List<BBTrack>
}

/**
 * Parses [Document] and generates [Sequence] of [Element] with tracks.
 */
interface HtmlTrackElementsParser {
    @Throws(ParseException::class)
    fun parse(document: Document): Elements
}

/**
 * Parses html [Element] as a [BBTrack]
 */
interface TrackElementParser {
    @Throws(ParseException::class)
    fun parse(element: Element): BBTrack
}

/**
 * Primitive [HtmlChartListParser] implementation via delegates
 */
class DelegateChartListParser(
    private val htmlTrackElementsParser: HtmlTrackElementsParser,
    private val trackElementParser: TrackElementParser
) : HtmlChartListParser {
    override fun parse(document: Document): List<BBTrack> =
        htmlTrackElementsParser.parse(document).map { trackElement ->
            trackElementParser.parse(trackElement)
        }.toList()
}

/**
 * [HtmlChartListParser] implementation to parse the [Document] via any of
 * supplied delegates.
 */
class CompositeChartListParser(
    private val delegates: List<HtmlChartListParser>
) : HtmlChartListParser {
    override fun parse(document: Document): List<BBTrack> = delegates.asSequence()
        .mapNotNull { delegate ->
            try {
                delegate.parse(document)
            } catch (e: ParseException) {
                e.printStackTrace()
                null
            }
        }.firstOrNull() ?: throw ParseException("There are no parsers for the given html", -1)
}
