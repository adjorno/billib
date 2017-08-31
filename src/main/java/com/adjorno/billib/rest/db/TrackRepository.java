package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TrackRepository extends CrudRepository<Track, Long> {

    @Query(value = "SELECT CT1.TRACK_ID FROM CHART_TRACK CT1\n" +
                   "INNER JOIN CHART_LIST ON CHART_LIST._ID = CT1.CHART_LIST_ID\n" +
                   "INNER JOIN WEEK ON WEEK.WEEK_ID = CHART_LIST.WEEK_ID\n" +
                   "INNER JOIN TRACK ON TRACK._ID = CT1.TRACK_ID\n" + "INNER JOIN\n" + "  (\n" +
                   "    SELECT TRACK_ID, MIN(WEEK.DATE) MINWEEK FROM CHART_TRACK CT2\n" +
                   "    INNER JOIN CHART_LIST ON CHART_LIST._ID = CT2.CHART_LIST_ID\n" +
                   "    INNER JOIN WEEK ON WEEK.WEEK_ID = CHART_LIST.WEEK_ID\n" + "    GROUP BY TRACK_ID\n" +
                   "  ) T ON CT1.TRACK_ID = T.TRACK_ID AND DATE = T.MINWEEK\n" + "WHERE DATE LIKE CONCAT(\"%-\", ?1)",
            nativeQuery = true)
    List<Long> findDebutsOfTheDay(String day);

    @Query(value = "SELECT TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, ARTIST._ID, ARTIST.NAME FROM GLOBAL_RANK_TRACK\n" +
                   "INNER JOIN TRACK ON TRACK._ID = GLOBAL_RANK_TRACK.TRACK_ID\n" +
                   "INNER JOIN ARTIST ON ARTIST._id = TRACK.ARTIST_ID\n" + "WHERE TRACK_ID IN (?1)\n" +
                   "ORDER BY GLOBAL_RANK_TRACK.RANK\n" + "LIMIT ?2", nativeQuery = true)
    List<Track> sortByGlobalRank(List<Long> ids, int size);

    Track findByMTitleAndMArtist(String trackTitle, Artist artist);

    @Query(value = "SELECT TRACK.TITLE FROM TRACK\n" + "WHERE ARTIST_ID IN (?1, ?2)\n" + "GROUP BY TRACK.TITLE\n" +
                   "HAVING COUNT(*) > 1\n", nativeQuery = true)
    List<String> findRepeatTitles(Long artistId1, Long artistId2);

    @Modifying
    @Query(value = "update Track t set t.mArtist = ?2 where t.mArtist = ?1")
    void updateArtists(Artist from, Artist to);

    List<Track> findBymArtist(Artist artist);

    @Query(value = "SELECT TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, ARTIST._ID, ARTIST.NAME FROM GLOBAL_RANK_TRACK\n" +
                   "INNER JOIN TRACK ON TRACK._ID = GLOBAL_RANK_TRACK.TRACK_ID\n" +
                   "INNER JOIN ARTIST ON ARTIST._id = TRACK.ARTIST_ID\n" + "WHERE RANK >= ?1 AND RANK < ?2\n" +
                   "ORDER BY GLOBAL_RANK_TRACK.RANK", nativeQuery = true)
    List<Track> findGlobalList(Long from, Long to);
}
