package com.adjorno.billib.rest.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChartListRepository extends CrudRepository<ChartList, Long> {

    ChartList findByChartAndWeek(Chart chart, Week week);

    @Query(value = "select cl from ChartList cl inner join cl.week w where cl.chart = ?1 order by w.date desc")
    Page<ChartList> findLast(Chart chart, Pageable pageable);

    @Query(value = "select cl from ChartList cl inner join cl.week w where cl.chart.id = ?1 order by w.date desc")
    Page<ChartList> findLast(Long chartId, Pageable pageable);

    @Query(value = "select cl from ChartList cl inner join cl.week w where cl.chart = ?1 and w.date > ?2 order by w.date asc")
    List<ChartList> findAfter(Chart chart, String date);

    @Modifying
    @Query(value = "update ChartList cl set cl.previousChartListId = ?2 where cl = ?1")
    void updatePreviousId(ChartList afterChartList, Long id);

    @Modifying
    @Query(value = "update ChartList cl set cl.number = ?2 where cl = ?1")
    void updateNumber(ChartList afterChartList, int i);
}
