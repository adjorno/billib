package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface GlobalRankArtistRepository extends CrudRepository<GlobalRankArtist, Long> {
    @Modifying
    @Query(value = "INSERT INTO GLOBAL_RANK_ARTIST (ARTIST_ID)\n" +
            "SELECT ARTIST._ID FROM CHART_LIST\n" +
            "JOIN CHART_TRACK ON CHART_LIST._ID = CHART_TRACK.CHART_LIST_ID\n" +
            "JOIN CHART ON CHART._ID = CHART_LIST.CHART_ID\n" +
            "JOIN TRACK ON TRACK._ID = CHART_TRACK.TRACK_ID\n" +
            "JOIN ARTIST ON ARTIST._ID = TRACK.ARTIST_ID\n" +
            "GROUP BY ARTIST_ID ORDER BY SUM((LIST_SIZE + 1 - RANK) * (LIST_SIZE + 1 - RANK)) DESC",
            nativeQuery = true)
    void refreshAll();

    @Modifying
    @Query(value = "INSERT INTO GLOBAL_RANK_ARTIST (ARTIST_ID)\n" +
            "SELECT ARTIST._ID \n" +
            "FROM ARTIST \n" +
            "WHERE ARTIST._ID NOT IN \n" +
            "(SELECT ARTIST_ID \n" +
            "FROM GLOBAL_RANK_ARTIST)",
            nativeQuery = true)
    void addMissing();

    GlobalRankArtist findByArtistId(Long artistId);
}
