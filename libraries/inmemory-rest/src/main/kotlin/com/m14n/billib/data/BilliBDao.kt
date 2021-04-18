package com.m14n.billib.data

import com.m14n.billib.data.artist.ArtistDao
import com.m14n.billib.data.artist.DuplicateArtistDao
import com.m14n.billib.data.chart.ChartDao
import com.m14n.billib.data.chartlist.ChartListDao
import com.m14n.billib.data.charttrack.ChartTrackDao
import com.m14n.billib.data.journal.JournalDao
import com.m14n.billib.data.track.DuplicateTrackDao
import com.m14n.billib.data.track.TrackDao


data class BilliBDao(
    val artistDao: ArtistDao,
    val duplicateArtistDao: DuplicateArtistDao,
    val trackDao: TrackDao,
    val duplicateTrackDao: DuplicateTrackDao,
    val journalDao: JournalDao,
    val chartDao: ChartDao,
    val chartListDao: ChartListDao,
    val chartTrackDao: ChartTrackDao
)
