package com.ifochka.billib.data.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.ifochka.billib.data.model.Artist
import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList
import com.ifochka.billib.data.model.ChartTrack
import com.ifochka.billib.data.model.Track
import com.ifochka.billib.db.BillibDatabase

interface ChartDatabaseRepository {
    suspend fun getAllCharts(): List<Chart>

    suspend fun getChartById(id: Long): Chart?

    suspend fun insertCharts(charts: List<Chart>)

    suspend fun clearCharts()

    suspend fun getChartListByDate(
        chartId: Long,
        weekDate: String,
    ): ChartList?

    suspend fun insertChartList(chartList: ChartList)

    suspend fun getChartsCachedAt(): Long?

    suspend fun getChartListCachedAt(
        chartId: Long,
        weekDate: String,
    ): Long?

    suspend fun updateTrackArtwork(
        trackId: Long,
        artworkUrl: String,
    )
}

@Suppress("TooManyFunctions") // Repository pattern requires many data access functions
class SqlDelightChartDatabase(
    private val database: BillibDatabase,
) : ChartDatabaseRepository {
    override suspend fun getAllCharts(): List<Chart> =
        database.chartQueries.selectAllCharts().awaitAsList().map { dbChart ->
            val journal =
                database.chartQueries.selectJournalById(dbChart.journal_id)
                    .awaitAsOneOrNull()?.toDomain()
            dbChart.toDomain(journal)
        }

    override suspend fun getChartById(id: Long): Chart? =
        database.chartQueries.selectChartById(id).awaitAsOneOrNull()?.let { dbChart ->
            val journal =
                database.chartQueries.selectJournalById(dbChart.journal_id)
                    .awaitAsOneOrNull()?.toDomain()
            dbChart.toDomain(journal)
        }

    override suspend fun insertCharts(charts: List<Chart>) =
        database.transaction {
            charts.forEach { chart ->
                val chartId = requireNotNull(chart.id) { "Cannot cache chart with null ID" }
                val chartName = requireNotNull(chart.name) { "Cannot cache chart with null name" }

                chart.journal?.let { journal ->
                    val journalId = requireNotNull(journal.id) { "Journal ID cannot be null" }
                    val journalName = requireNotNull(journal.name) { "Journal name cannot be null" }
                    database.chartQueries.insertJournal(
                        id = journalId,
                        name = journalName,
                    )
                }
                database.chartQueries.insertChart(
                    id = chartId,
                    name = chartName,
                    journal_id = requireNotNull(chart.journal?.id) { "Journal ID is required for chart '$chartName'" },
                    list_size = chart.listSize?.toLong(),
                    start_date = chart.startDate,
                    end_date = chart.endDate,
                    cached_at = currentTimeMillis(),
                )
            }
        }

    override suspend fun getChartListByDate(
        chartId: Long,
        weekDate: String,
    ): ChartList? =
        database.chartQueries.selectWeekByDate(weekDate).awaitAsOneOrNull()?.let { week ->
            database.chartQueries.selectChartListByChartAndWeek(chartId, week.id)
                .awaitAsOneOrNull()?.let { chartList ->
                    val chart = getChartById(chartId)
                    val chartTracks =
                        database.chartQueries.selectChartTracksWithDetailsById(chartList.id)
                            .awaitAsList().map { row ->
                                val artist =
                                    row.artist_id?.let {
                                        Artist(
                                            id = it,
                                            name = row.artist_name,
                                            nameNormalized = row.artist_name_normalized,
                                        )
                                    }
                                val track =
                                    Track(
                                        id = row.track_id,
                                        title = row.track_title,
                                        artist = artist,
                                        artistName = row.track_artist_name,
                                        firstChartDate = row.first_chart_date,
                                        peakGlobalRank = row.peak_global_rank?.toInt(),
                                        totalWeeksOnChart = row.total_weeks_on_chart?.toInt() ?: 0,
                                        artworkUrl = row.artwork_url,
                                    )
                                ChartTrack(
                                    track = track,
                                    rank = row.rank.toInt(),
                                    lastWeekRank = row.last_week_rank?.toInt() ?: 0,
                                )
                            }

                    ChartList(
                        id = chartList.id,
                        chart = chart,
                        week = week.toDomain(),
                        chartTracks = chartTracks,
                    )
                }
        }

    override suspend fun insertChartList(chartList: ChartList) {
        // Validate required fields first
        val chartListId = requireNotNull(chartList.id) { "ChartList ID cannot be null" }
        val chartId = requireNotNull(chartList.chart?.id) { "Chart ID in ChartList cannot be null" }
        val weekId = requireNotNull(chartList.week?.id) { "Week ID in ChartList cannot be null" }

        database.transaction {
            insertWeekData(weekId, chartList.week)
            insertChartData(chartId, chartList.chart)
            insertChartListData(chartListId, chartId, weekId)
            insertChartTracksData(chartListId, chartList.chartTracks)
        }
    }

    private suspend fun insertWeekData(
        weekId: Long,
        week: com.ifochka.billib.data.model.Week?,
    ) {
        requireNotNull(week) { "Week cannot be null" }
        val weekDate = requireNotNull(week.date) { "Week date cannot be null" }
        database.chartQueries.insertWeek(
            id = weekId,
            date = weekDate,
        )
    }

    private suspend fun insertChartData(
        chartId: Long,
        chart: Chart?,
    ) {
        requireNotNull(chart) { "Chart cannot be null" }
        val chartName = requireNotNull(chart.name) { "Chart name cannot be null" }

        chart.journal?.let { journal ->
            val journalId = requireNotNull(journal.id) { "Journal ID cannot be null" }
            val journalName = requireNotNull(journal.name) { "Journal name cannot be null" }
            database.chartQueries.insertJournal(
                id = journalId,
                name = journalName,
            )
        }

        database.chartQueries.insertChart(
            id = chartId,
            name = chartName,
            journal_id = requireNotNull(chart.journal?.id) { "Journal ID is required for chart '$chartName'" },
            list_size = chart.listSize?.toLong(),
            start_date = chart.startDate,
            end_date = chart.endDate,
            cached_at = currentTimeMillis(),
        )
    }

    private suspend fun insertChartListData(
        chartListId: Long,
        chartId: Long,
        weekId: Long,
    ) {
        database.chartQueries.insertChartList(
            id = chartListId,
            chart_id = chartId,
            week_id = weekId,
            cached_at = currentTimeMillis(),
        )
    }

    private suspend fun insertChartTracksData(
        chartListId: Long,
        chartTracks: List<ChartTrack>?,
    ) {
        chartTracks?.forEach { chartTrack ->
            val track = requireNotNull(chartTrack.track) { "Track cannot be null in ChartTrack" }
            val trackId = requireNotNull(track.id) { "Track ID cannot be null" }
            val trackTitle = requireNotNull(track.title) { "Track title cannot be null" }

            track.artist?.let { artist ->
                val artistId = requireNotNull(artist.id) { "Artist ID cannot be null" }
                val artistName = requireNotNull(artist.name) { "Artist name cannot be null" }
                database.chartQueries.insertArtist(
                    id = artistId,
                    name = artistName,
                    name_normalized = artist.nameNormalized,
                )
            }

            database.chartQueries.insertTrack(
                id = trackId,
                title = trackTitle,
                artist_id = track.artist?.id,
                artist_name = track.artistName,
                first_chart_date = track.firstChartDate,
                peak_global_rank = track.peakGlobalRank?.toLong(),
                total_weeks_on_chart = track.totalWeeksOnChart.toLong(),
            )

            database.chartQueries.insertChartTrack(
                chart_list_id = chartListId,
                track_id = trackId,
                rank = chartTrack.rank.toLong(),
                last_week_rank = chartTrack.lastWeekRank.toLong(),
            )
        }
    }

    override suspend fun clearCharts() =
        database.transaction {
            // Delete in order that respects foreign key constraints
            database.chartQueries.deleteAllChartTracks()
            database.chartQueries.deleteAllChartLists()
            database.chartQueries.deleteAllTrackArtworks()
            database.chartQueries.deleteAllTracks()
            database.chartQueries.deleteAllArtworks()
            database.chartQueries.deleteAllArtists()
            database.chartQueries.deleteAllWeeks()
            database.chartQueries.deleteAllCharts()
            database.chartQueries.deleteAllJournals()
        }

    override suspend fun getChartsCachedAt(): Long? =
        database.chartQueries.selectAllCharts().awaitAsList()
            .minOfOrNull { it.cached_at }

    override suspend fun getChartListCachedAt(
        chartId: Long,
        weekDate: String,
    ): Long? {
        val week = database.chartQueries.selectWeekByDate(weekDate).awaitAsOneOrNull() ?: return null
        return database.chartQueries.selectChartListByChartAndWeek(chartId, week.id)
            .awaitAsOneOrNull()?.cached_at
    }

    override suspend fun updateTrackArtwork(
        trackId: Long,
        artworkUrl: String,
    ) {
        database.transaction {
            database.chartQueries.insertArtwork(url = artworkUrl)
            val artwork = database.chartQueries.selectArtworkByUrl(url = artworkUrl)
                .awaitAsOneOrNull() ?: return@transaction
            database.chartQueries.insertTrackArtwork(
                track_id = trackId,
                artwork_id = artwork.id,
            )
        }
    }
}
