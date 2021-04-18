package com.m14n.billib.data.chartlist

import com.m14n.billib.data.chart.Chart
import com.m14n.billib.data.dao.Dao
import com.m14n.billib.data.dao.collections.collectionsDao
import com.m14n.billib.data.week.Week

fun chartListDao(chartLists: Collection<ChartList>) = ChartListDaoDelegate(
    collectionsDao(chartLists),
    FindByChartAndWeekByRelation()
)

interface ChartListDao :
    Dao<ChartList>,
    FindByChartAndWeek

class ChartListDaoDelegate(
    dao: Dao<ChartList>,
    findByChartAndWeek: FindByChartAndWeek
) : ChartListDao,
    Dao<ChartList> by dao,
    FindByChartAndWeek by findByChartAndWeek

interface FindByChartAndWeek {
    fun findByChartAndWeek(chart: Chart, week: Week): ChartList?
}

class FindByChartAndWeekByRelation : FindByChartAndWeek {
    override fun findByChartAndWeek(chart: Chart, week: Week) =
        chart.chartLists?.find { it.week == week }
}
