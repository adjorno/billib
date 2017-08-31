package com.adjorno.billib.rest.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChartListRepository extends CrudRepository<ChartList, Long> {

    ChartList findByMChartAndMWeek(Chart chart, Week week);

    @Query(value = "select cl from ChartList cl inner join cl.mWeek w where cl.mChart = ?1 order by w.mDate desc")
    Page<ChartList> findLast(Chart chart, Pageable pageable);

    @Query(value = "select cl from ChartList cl inner join cl.mWeek w where cl.mChart.mId = ?1 order by w.mDate desc")
    Page<ChartList> findLast(Long chartId, Pageable pageable);

    @Query(value = "select cl from ChartList cl inner join cl.mWeek w where cl.mChart = ?1 and w.mDate > ?2 order by w.mDate asc")
    List<ChartList> findAfter(Chart chart, String date);

    @Modifying
    @Query(value = "update ChartList cl set cl.mPreviousChartListId = ?2 where cl = ?1")
    void updatePreviousId(ChartList afterChartList, Long id);

    @Modifying
    @Query(value = "update ChartList cl set cl.mNumber = ?2 where cl = ?1")
    void updateNumber(ChartList afterChartList, int i);
}
