package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface GlobalRankTrackRepository extends CrudRepository<GlobalRankTrack, Long> {

    @Modifying
    @Query(value = "INSERT INTO GLOBAL_RANK_TRACK (TRACK_ID)\n" +
                   "SELECT TRACK._ID FROM CHART_LIST\n" +
                   "JOIN CHART_TRACK ON CHART_LIST._ID = CHART_TRACK.CHART_LIST_ID\n" +
                   "JOIN CHART ON CHART._ID = CHART_LIST.CHART_ID\n" +
                   "JOIN TRACK ON TRACK._ID = CHART_TRACK.TRACK_ID\n" +
                   "JOIN ARTIST ON ARTIST._ID = TRACK.ARTIST_ID\n" + "GROUP BY TRACK_ID\n" +
                   "ORDER BY SUM((LIST_SIZE + 1 - _RANK) * (LIST_SIZE + 1 - _RANK)) DESC,\n" + "ARTIST.NAME, TRACK.TITLE",
            nativeQuery = true)
    void refreshAll();

    GlobalRankTrack findByTrackId(Long trackId);
}
