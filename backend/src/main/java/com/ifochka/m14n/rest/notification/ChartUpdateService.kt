package com.ifochka.m14n.rest.notification

import com.ifochka.m14n.rest.db.Artist
import com.ifochka.m14n.rest.db.ArtistRepository
import com.ifochka.m14n.rest.db.ChartList
import com.ifochka.m14n.rest.db.ChartListRepository
import com.ifochka.m14n.rest.db.ChartRepository
import com.ifochka.m14n.rest.db.ChartTrack
import com.ifochka.m14n.rest.db.ChartTrackRepository
import com.ifochka.m14n.rest.db.Track
import com.ifochka.m14n.rest.db.TrackRepository
import com.ifochka.m14n.rest.db.Week
import com.ifochka.m14n.rest.db.WeekRepository
import com.m14n.data.billboard.BB
import com.m14n.data.billboard.html.BBHtmlParser
import com.m14n.data.billboard.model.BBChartMetadata
import com.m14n.data.billboard.model.BBJournalMetadata
import com.m14n.data.billboard.parser.defaultChartListParser
import defaultDateParser
import jakarta.persistence.EntityManager
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Service
class ChartUpdateService(
    transactionManager: PlatformTransactionManager,
    private val entityManager: EntityManager,
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository,
    private val chartRepository: ChartRepository,
    private val weekRepository: WeekRepository,
    private val chartListRepository: ChartListRepository,
    private val chartTrackRepository: ChartTrackRepository,
) {
    private val logger = LoggerFactory.getLogger(ChartUpdateService::class.java)
    private val transactionTemplate = TransactionTemplate(transactionManager)
    private val dateParser = defaultDateParser()
    private val tracksParser = defaultChartListParser()
    private val metadata: BBJournalMetadata by lazy { loadMetadata() }

    fun checkForNewCharts(): ChartUpdateResult {
        val charts = metadata.charts.filter { it.endDate == null }
        logger.info("Starting chart update check for ${charts.size} charts")
        val newCharts = mutableListOf<String>()
        val skippedCharts = mutableListOf<String>()
        val failedCharts = mutableMapOf<String, String>()
        charts.forEach { chartMeta ->
            logger.info("Checking: ${chartMeta.name}")
            runCatching { transactionTemplate.execute { importChart(chartMeta) } }
                .onSuccess { name ->
                    if (name != null) newCharts.add(name) else skippedCharts.add(chartMeta.name)
                }
                .onFailure { error ->
                    val errorType = when {
                        error is java.io.IOException || error.cause is java.io.IOException -> "Network"
                        error.message?.contains("429") == true -> "RateLimit"
                        else -> error.javaClass.simpleName
                    }
                    logger.error("[$errorType] Failed '${chartMeta.name}': ${error.message}")
                    failedCharts[chartMeta.name] = "[$errorType] ${error.message}"
                }
        }
        logger.info(
            "Chart update done — new: ${newCharts.size}, skipped: ${skippedCharts.size}, failed: ${failedCharts.size}",
        )
        if (newCharts.isNotEmpty()) refreshGlobalRankings()
        return ChartUpdateResult(
            newCharts = newCharts,
            skippedCharts = skippedCharts,
            failedCharts = failedCharts,
        )
    }

    @Transactional
    fun refreshGlobalRankings() {
        entityManager.createNativeQuery("SELECT refresh_global_rankings()").singleResult
        logger.info("Global rankings refreshed")
    }

    private fun importChart(chartMeta: BBChartMetadata): String? {
        val chart = chartRepository.findByName(chartMeta.name) ?: run {
            logger.warn("'${chartMeta.name}' not found in DB, skipping")
            return null
        }
        val document = BBHtmlParser.getChartDocument(
            metadata = metadata,
            chart = chartMeta,
        )
        val dateStr = BB.CHART_DATE_FORMAT.format(dateParser.parse(document))
        val week = weekRepository.findByDate(dateStr) ?: weekRepository.save(Week(date = dateStr))
        if (chartListRepository.findByChartAndWeek(chart, week) != null) {
            logger.info("'${chartMeta.name}' already up to date ($dateStr), skipping")
            return null
        }
        val chartList = chartListRepository.save(ChartList(chart = chart, week = week))
        val tracks = tracksParser.parse(document)
        logger.info("'${chartMeta.name}' — new week $dateStr, importing ${tracks.size} tracks")
        tracks.forEach { bbTrack ->
            val artist = artistRepository.findByName(bbTrack.artist.trim())
                ?: artistRepository.save(Artist(name = bbTrack.artist.trim()))
            val track = trackRepository.findByTitleAndArtist(bbTrack.title.trim(), artist)
                ?: trackRepository.save(
                    Track(
                        title = bbTrack.title.trim(),
                        artist = artist,
                        artistName = artist.name,
                    ),
                )
            chartTrackRepository.save(
                ChartTrack(
                    track = track,
                    chartList = chartList,
                    rank = bbTrack.rank,
                    lastWeekRank = BB.extractLastWeekRank(bbTrack.positionInfo?.lastWeek ?: "-"),
                ),
            )
        }
        logger.info("Imported '${chartMeta.name}' ($dateStr, chartListId=${chartList.id})")
        return chartMeta.name
    }

    private fun loadMetadata(): BBJournalMetadata {
        val json = ClassPathResource("metadata_billboard.json").inputStream.bufferedReader().readText()
        return Json.decodeFromString(json)
    }
}

data class ChartUpdateResult(
    val newCharts: List<String>,
    val skippedCharts: List<String>,
    val failedCharts: Map<String, String>,
)
