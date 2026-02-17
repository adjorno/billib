package com.ifochka.billib.data.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.ifochka.billib.data.model.Chart
import com.ifochka.billib.data.model.ChartList
import com.ifochka.billib.db.BillibDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ChartDatabaseRepository {
    suspend fun getAllCharts(): List<Chart>

    suspend fun getChartById(id: Long): Chart?

    suspend fun insertCharts(charts: List<Chart>)

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
                    chart.journal?.let { journal ->
                        database.chartQueries.insertJournal(
                            id = journal.id ?: 0,
                            name = journal.name ?: "",
                        )
                    }
                    database.chartQueries.insertChart(
                        id = chart.id ?: 0,
                        name = chart.name ?: "",
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
                database.chartQueries.selectChartTracksByListId(chartList.id)
                    .awaitAsList().map { dbChartTrack ->
                        val track =
                            database.chartQueries.selectTrackById(dbChartTrack.track_id)
                                .awaitAsOneOrNull()
                        val artist =
                            track?.artist_id?.let {
                                database.chartQueries.selectArtistById(it).awaitAsOneOrNull()
                            }?.toDomain()
                        dbChartTrack.toDomain(track?.toDomain(artist))
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
            database.transaction {
                chartList.week?.let { week ->
                    database.chartQueries.insertWeek(
                        id = week.id ?: 0,
                        date = week.date ?: "",
                    )
                }

                chartList.chart?.let { chart ->
                    chart.journal?.let { journal ->
                        database.chartQueries.insertJournal(
                            id = journal.id ?: 0,
                            name = journal.name ?: "",
                        )
                    }
                    database.chartQueries.insertChart(
                        id = chart.id ?: 0,
                        name = chart.name ?: "",
                        journal_id = chart.journal?.id ?: 0,
                        list_size = chart.listSize?.toLong(),
                        start_date = chart.startDate,
                        end_date = chart.endDate,
                        cached_at = currentTimeMillis(),
                    )
                }

                database.chartQueries.insertChartList(
                    id = chartList.id ?: 0,
                    chart_id = chartList.chart?.id ?: 0,
                    week_id = chartList.week?.id ?: 0,
                    cached_at = currentTimeMillis(),
                )

                chartList.chartTracks?.forEach { chartTrack ->
                    chartTrack.track?.let { track ->
                        track.artist?.let { artist ->
                            database.chartQueries.insertArtist(
                                id = artist.id ?: 0,
                                name = artist.name ?: "",
                                name_normalized = artist.nameNormalized,
                            )
                        }
                        database.chartQueries.insertTrack(
                            id = track.id ?: 0,
                            title = track.title ?: "",
                            artist_id = track.artist?.id,
                            artist_name = track.artistName,
                            first_chart_date = track.firstChartDate,
                            peak_global_rank = track.peakGlobalRank?.toLong(),
                            total_weeks_on_chart = track.totalWeeksOnChart.toLong(),
                        )
                        database.chartQueries.insertChartTrack(
                            chart_list_id = chartList.id ?: 0,
                            track_id = track.id ?: 0,
                            rank = chartTrack.rank.toLong(),
                            last_week_rank = chartTrack.lastWeekRank.toLong(),
                        )
                    }
                }
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
