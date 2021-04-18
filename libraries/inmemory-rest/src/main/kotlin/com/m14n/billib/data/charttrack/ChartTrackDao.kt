package com.m14n.billib.data.charttrack

import com.m14n.billib.data.chartlist.ChartList
import com.m14n.billib.data.dao.Dao
import com.m14n.billib.data.dao.collections.collectionsDao

fun chartTrackDao(chartTracks: Collection<ChartTrack>) = ChartTrackDaoDelegate(
    collectionsDao(chartTracks),
    FindByChartListAndRankByRelation()
)

interface ChartTrackDao :
    Dao<ChartTrack>,
    FindByChartListAndRank

class ChartTrackDaoDelegate(
    dao: Dao<ChartTrack>,
    findByChartListAndRank: FindByChartListAndRank
) : ChartTrackDao,
    Dao<ChartTrack> by dao,
    FindByChartListAndRank by findByChartListAndRank

interface FindByChartListAndRank {
    fun findByChartListAndRank(chartList: ChartList, rank: Int): ChartTrack?
}

class FindByChartListAndRankByRelation : FindByChartListAndRank {
    override fun findByChartListAndRank(chartList: ChartList, rank: Int) =
        chartList.chartTracks?.find { it.rank == rank }
}
