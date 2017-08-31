package com.adjorno.billib.rest.db;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChartTrackRepository extends CrudRepository<ChartTrack, Long> {

    List<ChartTrack> findBymChartList(ChartList chartList);

    ChartTrack findBymTrackAndMChartList(Track track, ChartList chartList);

    @Query(value = "SELECT RANK FROM CHART_TRACK\n" +
                   "                INNER JOIN TRACK ON TRACK._ID = CHART_TRACK.TRACK_ID\n" +
                   "                INNER JOIN CHART_LIST ON  CHART_TRACK.CHART_LIST_ID = CHART_LIST._ID\n" +
                   "                WHERE CHART_TRACK.CHART_LIST_ID IN (\n" +
                   "                SELECT CHART_LIST.PREVIOUS_CHART_LIST_ID FROM CHART_LIST\n" +
                   "                INNER JOIN CHART_TRACK ON  CHART_TRACK.CHART_LIST_ID = CHART_LIST._ID\n" +
                   "                WHERE CHART_TRACK._ID = :id)\n" + "                AND TRACK_ID IN (\n" +
                   "                SELECT TRACK_ID FROM CHART_TRACK\n" +
                   "                WHERE CHART_TRACK._ID = :id)", nativeQuery = true)
    List<Integer> findPreviousWeekRank(@Param(value = "id") Long mChartTrackId);

    @Query(value =
            "select ct, (c.mListSize - ct.mRank)*(c.mListSize - ct.mRank) - c.mListSize as rating from ChartTrack ct " +
            "inner join ct.mChartList cl inner join cl.mWeek w inner join cl.mChart c " +
            "where w.mId = ?1 and ct.mLastWeekRank = 0")
    List<Object[]> findDebuts(Long weekId);

    @Query(value = "select ct from ChartTrack ct inner join ct.mChartList cl where cl.mId = ?1 and ct.mRank = ?2")
    List<ChartTrack> findByChartListIdAndRank(Long id, int rank);

    @Modifying
    @Query(value = "update ChartTrack ct set ct.mTrack = ?2 where ct.mTrack = ?1")
    void updateTracks(Track from, Track to);

    @Modifying
    @Query(value = "update ChartTrack ct set ct.mRank = ?2 where ct = ?1")
    void updateRank(ChartTrack sameTrack, int rank);

    @Modifying
    @Query(value = "update ChartTrack ct set ct.mLastWeekRank = ?2 where ct = ?1")
    void updateLastWeekRank(ChartTrack sameTrack, int lastWeekRank);

    @Modifying
    @Query(value = "update ChartTrack ct set ct.mTrack = ?2 where ct = ?1")
    void updateTrack(ChartTrack chartTrack, Track track);

    @Query(value = "select ct from ChartTrack ct where ct.mChartList = ?1")
    List<ChartTrack> countRealChartListSize(ChartList previousChartList);

    @Query(value = "select ct from ChartTrack ct inner join ct.mChartList cl inner join cl.mWeek w where ct.mTrack = ?1")
    List<ChartTrack> findByTrackAndSort(Track track, Sort sort);

    @Query(value = "select ct from ChartTrack ct inner join ct.mChartList cl where cl.mChart = ?2 and ct.mTrack = ?1")
    List<ChartTrack> findByTrackAndChart(Chart chart, Track track);

    List<ChartTrack> findBymTrack(Track track);

    @Query(value = "select ct from ChartTrack ct inner join ct.mChartList cl where cl.mChart in ?2 and ct.mTrack = ?1")
    List<ChartTrack> findByTrackInCharts(Track track, Iterable<Chart> charts);

    @Query(value = "select ct from ChartTrack ct inner join ct.mChartList cl where cl.mWeek = ?1")
    List<ChartTrack> findByWeek(Week week);
}
