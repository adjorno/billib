package com.m14n.billib.reader

import com.m14n.billib.data.BilliBDao
import com.m14n.billib.data.artist.Artist
import com.m14n.billib.data.artist.ArtistDao
import com.m14n.billib.data.artist.artistDao
import com.m14n.billib.data.artist.duplicateArtistDao
import com.m14n.billib.data.billboard.date
import com.m14n.billib.data.chart.Chart
import com.m14n.billib.data.chart.ChartDao
import com.m14n.billib.data.chart.chartDao
import com.m14n.billib.data.chartlist.ChartList
import com.m14n.billib.data.chartlist.ChartListDao
import com.m14n.billib.data.chartlist.chartListDao
import com.m14n.billib.data.charttrack.ChartTrack
import com.m14n.billib.data.charttrack.chartTrackDao
import com.m14n.billib.data.dao.requestById
import com.m14n.billib.data.journal.Journal
import com.m14n.billib.data.journal.JournalDao
import com.m14n.billib.data.journal.journalDao
import com.m14n.billib.data.track.Track
import com.m14n.billib.data.track.TrackDao
import com.m14n.billib.data.track.collectionTrackDao
import com.m14n.billib.data.track.duplicateTrackDao
import com.m14n.billib.data.week.Week
import com.m14n.billib.data.week.WeekDao
import com.m14n.billib.data.week.weekDao
import java.sql.Statement
import javax.sql.DataSource

interface DaoReader<T> {
    fun read(source: T): BilliBDao
}

class SqlDataSourceReader : DaoReader<DataSource> {
    override fun read(source: DataSource): BilliBDao {
        return source.connection.use { conn ->
            conn.createStatement().use { stmt ->
                val artists = readArtists(stmt)
                val artistDao = artistDao(artists)

                val duplicateArtist = readDuplicateArtists(stmt, artistDao)

                val tracks = readTracks(stmt, artistDao)
                val trackDao = collectionTrackDao(tracks)
                val duplicateTrack = readDuplicateTracks(stmt, trackDao)

                val journals = readJournals(stmt)
                val journalDao = journalDao(journals)

                val charts = readCharts(stmt, journalDao)
                charts.groupBy { it.journal.id }.forEach {
                    journalDao.findById(it.key)?.charts = it.value
                }
                val chartDao = chartDao(charts)

                val weeks = readWeeks(stmt)
                val weekDao = weekDao(weeks)

                val chartLists = readChartLists(stmt, chartDao, weekDao)
                chartLists.groupBy { it.chart.id }.forEach {
                    chartDao.findById(it.key)?.chartLists = it.value
                }
                val chartListDao = chartListDao(chartLists)

                val chartTracks = readChartTracks(stmt, chartListDao, trackDao)
                chartTracks.groupBy { it.chartList?.id }.forEach {
                    chartListDao.findById(it.key)?.chartTracks = it.value
                }
                val chartTrackDao = chartTrackDao(chartTracks)

                BilliBDao(
                    artistDao,
                    duplicateArtistDao(duplicateArtist),
                    trackDao,
                    duplicateTrackDao(duplicateTrack),
                    journalDao,
                    chartDao,
                    chartListDao,
                    chartTrackDao
                )
            }
        }
    }

    private fun readChartTracks(
        stmt: Statement,
        chartListDao: ChartListDao,
        trackDao: TrackDao
    ) = stmt.executeQuery("SELECT * FROM CHART_TRACK").use { result ->
        generateSequence { result.takeIf { result.next() } }.mapNotNull {
            // There might be some leftovers from UK albums charts
            chartListDao.findById(result.getLong("CHART_LIST_ID"))?.let { chartList ->
                ChartTrack(
                    id = result.getLong("_id"),
                    chartList = chartList,
                    rank = result.getInt("_RANK"),
                    track = trackDao.requestById(result.getLong("TRACK_ID"))
                )
            }
        }.toList()
    }

    private fun readChartLists(
        stmt: Statement,
        chartDao: ChartDao,
        weekDao: WeekDao
    ) = stmt.executeQuery("SELECT * FROM CHART_LIST").use { result ->
        generateSequence { result.takeIf { result.next() } }.map {
            ChartList(
                id = result.getLong("_id"),
                chart = chartDao.requestById(result.getLong("CHART_ID")),
                week = weekDao.requestById(result.getLong("WEEK_ID"))
            )
        }.toList()
    }

    private fun readWeeks(stmt: Statement) =
        stmt.executeQuery("SELECT * FROM WEEK").use { result ->
            generateSequence { result.takeIf { result.next() } }.map {
                Week(
                    id = result.getLong("WEEK_ID"),
                    date = result.getString("DATE").date
                )
            }.toList()
        }

    private fun readCharts(
        stmt: Statement,
        journalDao: JournalDao
    ) = stmt.executeQuery("SELECT * FROM CHART").use { result ->
        generateSequence { result.takeIf { result.next() } }.map {
            val endDate = result.getString("END_DATE")
            Chart(
                id = result.getLong("_id"),
                name = result.getString("NAME"),
                journal = journalDao.requestById(result.getLong("JOURNAL_ID")),
                listSize = result.getInt("LIST_SIZE"),
                startDate = result.getString("START_DATE").date,
                endDate = endDate?.date
            )
        }.toList()
    }

    private fun readJournals(stmt: Statement) =
        stmt.executeQuery("SELECT * FROM JOURNAL").use { result ->
            generateSequence { result.takeIf { result.next() } }.map {
                Journal(
                    result.getLong("_id"),
                    result.getString("NAME")
                )
            }.toList()
        }

    private fun readTracks(
        stmt: Statement,
        artistDao: ArtistDao
    ) = stmt.executeQuery("SELECT * FROM TRACK").use { result ->
        generateSequence { result.takeIf { result.next() } }.map {
            Track(
                result.getLong("_id"),
                result.getString("TITLE"),
                artistDao.requestById(result.getLong("ARTIST_ID"))
            )
        }.toList()
    }

    private fun readArtists(stmt: Statement) =
        stmt.executeQuery("SELECT * FROM ARTIST").use { result ->
            generateSequence { result.takeIf { result.next() } }.map {
                Artist(result.getLong("_id"), result.getString("NAME"))
            }.toList()
        }

    private fun readDuplicateArtists(stmt: Statement, artistDao: ArtistDao) =
        stmt.executeQuery("SELECT * FROM DUPLICATE_ARTIST").use { result ->
            generateSequence { result.takeIf { result.next() } }.map {
                result.getString("DUPLICATE_NAME") to artistDao.findById(result.getLong("ARTIST_ID"))!!
            }.toMap()
        }

    private fun readDuplicateTracks(stmt: Statement, trackDao: TrackDao) =
        stmt.executeQuery("SELECT * FROM DUPLICATE_TRACK").use { result ->
            generateSequence { result.takeIf { result.next() } }.map {
                result.getString("DUPLICATE_TITLE") to trackDao.findById(result.getLong("TRACK_ID"))!!
            }.toMap()
        }
}
