package com.ifochka.billib.rest.db

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ChartListRepository : CrudRepository<ChartList, Long> {

    fun findByChartAndWeek(chart: Chart, week: Week): ChartList?

    @Query(value = "select cl from ChartList cl inner join cl.week w where cl.chart = ?1 order by w.date desc")
    fun findLast(chart: Chart, pageable: Pageable): Page<ChartList>

    @Query(value = "select cl from ChartList cl inner join cl.week w where cl.chart.id = ?1 order by w.date desc")
    fun findLast(chartId: Long, pageable: Pageable): Page<ChartList>

    @Query(value = "select cl from ChartList cl inner join cl.week w where cl.chart = ?1 and w.date > ?2 order by w.date asc")
    fun findAfter(chart: Chart, date: String): List<ChartList>

    @Modifying
    @Query(value = "update ChartList cl set cl.previousChartListId = ?2 where cl = ?1")
    fun updatePreviousId(afterChartList: ChartList, id: Long)

    @Modifying
    @Query(value = "update ChartList cl set cl.number = ?2 where cl = ?1")
    fun updateNumber(afterChartList: ChartList, i: Int)
}
