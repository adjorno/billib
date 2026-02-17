package com.ifochka.billib.data.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.ifochka.billib.data.model.Artist
import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList
import com.ifochka.billib.data.model.ChartTrack
import com.ifochka.billib.data.model.Track
import com.ifochka.billib.db.BillibDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
}

class SqlDelightChartDatabase(
    private val database: BillibDatabase,
) : ChartDatabaseRepository {
    override suspend fun getAllCharts(): List<Chart> =
        withContext(Dispatchers.Default) {
            database.chartQueries.selectAllCharts().awaitAsList().map { dbChart ->
                val journal =
                    database.chartQueries.selectJournalById(dbChart.journal_id)
                        .awaitAsOneOrNull()?.toDomain()
                dbChart.toDomain(journal)
            }
        }

    override suspend fun getChartById(id: Long): Chart? =
        withContext(Dispatchers.Default) {
            database.chartQueries.selectChartById(id).awaitAsOneOrNull()?.let { dbChart ->
                val journal =
                    database.chartQueries.selectJournalById(dbChart.journal_id)
                        .awaitAsOneOrNull()?.toDomain()
                dbChart.toDomain(journal)
            }
        }

    override suspend fun insertCharts(charts: List<Chart>) =
        withContext(Dispatchers.Default) {
            database.transaction {
                charts.forEach { chart ->
                    val chartId = chart.id ?: return@forEach
                    val chartName = chart.name ?: return@forEach

                    chart.journal?.let { journal ->
                        val journalId = journal.id ?: return@let
                        val journalName = journal.name ?: return@let
                        database.chartQueries.insertJournal(
                            id = journalId,
                            name = journalName,
                        )
                    }
                    database.chartQueries.insertChart(
                        id = chartId,
                        name = chartName,
                        journal_id = chart.journal?.id ?: 0,
                        list_size = chart.listSize?.toLong(),
                        start_date = chart.startDate,
                        end_date = chart.endDate,
                        cached_at = currentTimeMillis(),
                    )
                }
            }
        }

    override suspend fun getChartListByDate(
        chartId: Long,
        weekDate: String,
    ): ChartList? =
        withContext(Dispatchers.Default) {
            val week = database.chartQueries.selectWeekByDate(weekDate).awaitAsOneOrNull() ?: return@withContext null
            val chartList =
                database.chartQueries.selectChartListByChartAndWeek(chartId, week.id)
                    .awaitAsOneOrNull() ?: return@withContext null

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

    override suspend fun insertChartList(chartList: ChartList) =
        withContext(Dispatchers.Default) {
            // Validate required fields first
            val chartListId = chartList.id ?: return@withContext
            val chartId = chartList.chart?.id ?: return@withContext
            val weekId = chartList.week?.id ?: return@withContext

            database.transaction {
                chartList.week.let { week ->
                    val weekDate = week.date ?: return@let
                    database.chartQueries.insertWeek(
                        id = weekId,
                        date = weekDate,
                    )
                }

                chartList.chart.let { chart ->
                    val chartName = chart.name ?: return@let
                    chart.journal?.let { journal ->
                        val journalId = journal.id ?: return@let
                        val journalName = journal.name ?: return@let
                        database.chartQueries.insertJournal(
                            id = journalId,
                            name = journalName,
                        )
                    }
                    database.chartQueries.insertChart(
                        id = chartId,
                        name = chartName,
                        journal_id = chart.journal?.id ?: 0,
                        list_size = chart.listSize?.toLong(),
                        start_date = chart.startDate,
                        end_date = chart.endDate,
                        cached_at = currentTimeMillis(),
                    )
                }

                database.chartQueries.insertChartList(
                    id = chartListId,
                    chart_id = chartId,
                    week_id = weekId,
                    cached_at = currentTimeMillis(),
                )

                chartList.chartTracks?.forEach { chartTrack ->
                    chartTrack.track?.let { track ->
                        val trackId = track.id ?: return@forEach
                        val trackTitle = track.title ?: return@forEach

                        track.artist?.let { artist ->
                            val artistId = artist.id ?: return@let
                            val artistName = artist.name ?: return@let
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
            }
        }

    override suspend fun clearCharts() =
        withContext(Dispatchers.Default) {
            database.transaction {
                database.chartQueries.deleteAllCharts()
                database.chartQueries.deleteAllJournals()
            }
        }

    override suspend fun getChartsCachedAt(): Long? =
        withContext(Dispatchers.Default) {
            database.chartQueries.selectAllCharts().awaitAsList()
                .minOfOrNull { it.cached_at }
        }

    override suspend fun getChartListCachedAt(
        chartId: Long,
        weekDate: String,
    ): Long? =
        withContext(Dispatchers.Default) {
            val week = database.chartQueries.selectWeekByDate(weekDate).awaitAsOneOrNull() ?: return@withContext null
            database.chartQueries.selectChartListByChartAndWeek(chartId, week.id)
                .awaitAsOneOrNull()?.cached_at
        }
}
