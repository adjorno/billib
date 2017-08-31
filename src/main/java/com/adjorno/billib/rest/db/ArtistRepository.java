package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ArtistRepository extends CrudRepository<Artist, Long> {
    Artist findBymName(String artistName);

    @Modifying
    @Query(value = "update Artist a set a.mName = ?2 where a = ?1")
    void rename(Artist artist, String newName);

    @Query(value = "SELECT ARTIST._ID, ARTIST.NAME FROM GLOBAL_RANK_ARTIST\n" +
                   "INNER JOIN ARTIST ON ARTIST._ID = GLOBAL_RANK_ARTIST.ARTIST_ID\n" + "WHERE ARTIST_ID IN (?1)\n" +
                   "ORDER BY GLOBAL_RANK_ARTIST.RANK\n" + "LIMIT ?2", nativeQuery = true)
    List<Artist> sortByGlobalRank(List<Long> ids, int size);

    @Query(value = "SELECT ARTIST._ID, ARTIST.NAME FROM GLOBAL_RANK_ARTIST\n" +
                   "INNER JOIN ARTIST ON ARTIST._ID = GLOBAL_RANK_ARTIST.ARTIST_ID\n" +
                   "WHERE RANK >= ?1 AND RANK < ?2\n" + "ORDER BY GLOBAL_RANK_ARTIST.RANK", nativeQuery = true)
    List<Artist> findGlobalList(Long from, Long to);

    List<Artist> findBymNameLike(String name);
}