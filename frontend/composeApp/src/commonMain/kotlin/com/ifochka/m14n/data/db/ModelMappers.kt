@file:Suppress("TooManyFunctions") // Model mapping requires many conversion functions

package com.ifochka.m14n.data.db

import com.ifochka.m14n.data.model.Artist
import com.ifochka.m14n.data.model.Chart
import com.ifochka.m14n.data.model.ChartTrack
import com.ifochka.m14n.data.model.Journal
import com.ifochka.m14n.data.model.Track
import com.ifochka.m14n.data.model.Week
import com.ifochka.m14n.db.Artist as DbArtist
import com.ifochka.m14n.db.Chart as DbChart
import com.ifochka.m14n.db.Chart_track as DbChartTrack
import com.ifochka.m14n.db.Journal as DbJournal
import com.ifochka.m14n.db.Track as DbTrack
import com.ifochka.m14n.db.Week as DbWeek

fun DbArtist.toDomain(): Artist =
    Artist(
        id = id,
        name = name,
        nameNormalized = name_normalized,
    )

fun DbJournal.toDomain(): Journal =
    Journal(
        id = id,
        name = name,
    )

fun DbWeek.toDomain(): Week =
    Week(
        id = id,
        date = date,
    )

fun DbChart.toDomain(journal: Journal?): Chart =
    Chart(
        id = id,
        name = name,
        journal = journal,
        listSize = list_size?.toInt(),
        startDate = start_date,
        endDate = end_date,
    )

fun DbTrack.toDomain(artist: Artist?): Track =
    Track(
        id = id,
        title = title,
        artist = artist,
        artistName = artist_name,
        firstChartDate = first_chart_date,
        peakGlobalRank = peak_global_rank?.toInt(),
        totalWeeksOnChart = total_weeks_on_chart?.toInt() ?: 0,
    )

fun DbChartTrack.toDomain(track: Track?): ChartTrack =
    ChartTrack(
        track = track,
        rank = rank.toInt(),
        lastWeekRank = last_week_rank?.toInt() ?: 0,
    )

fun Artist.toDb(): DbArtist =
    DbArtist(
        id = id ?: 0,
        name = name ?: "",
        name_normalized = nameNormalized,
    )

fun Journal.toDb(): DbJournal =
    DbJournal(
        id = id ?: 0,
        name = name ?: "",
    )

fun Week.toDb(): DbWeek =
    DbWeek(
        id = id ?: 0,
        date = date ?: "",
    )

fun Chart.toDb(): DbChart =
    DbChart(
        id = id ?: 0,
        name = name ?: "",
        journal_id = journal?.id ?: 0,
        list_size = listSize?.toLong(),
        start_date = startDate,
        end_date = endDate,
        cached_at = currentTimeMillis(),
    )

fun Track.toDb(): DbTrack =
    DbTrack(
        id = id ?: 0,
        title = title ?: "",
        artist_id = artist?.id,
        artist_name = artistName,
        first_chart_date = firstChartDate,
        peak_global_rank = peakGlobalRank?.toLong(),
        total_weeks_on_chart = totalWeeksOnChart.toLong(),
    )
