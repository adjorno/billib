package com.ifochka.billib.rest.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ArtistRepository : CrudRepository<Artist, Long> {
    fun findByName(artistName: String): Artist?

    @Modifying
    @Query(value = "update Artist a set a.name = ?2 where a = ?1")
    fun rename(
        artist: Artist,
        newName: String,
    )

    @Query(
        value = "SELECT ARTIST._ID, ARTIST.NAME, ARTIST.NAME_NORMALIZED FROM GLOBAL_RANK_ARTIST\n" +
            "INNER JOIN ARTIST ON ARTIST._ID = GLOBAL_RANK_ARTIST.ARTIST_ID\n" +
            "WHERE ARTIST_ID IN (?1)\n" +
            "ORDER BY GLOBAL_RANK_ARTIST.RANK\n" + "LIMIT ?2",
        nativeQuery = true,
    )
    fun sortByGlobalRank(
        ids: List<Long>,
        size: Int,
    ): List<Artist>

    @Query(
        value = "SELECT ARTIST._ID, ARTIST.NAME, ARTIST.NAME_NORMALIZED FROM GLOBAL_RANK_ARTIST\n" +
            "INNER JOIN ARTIST ON ARTIST._ID = GLOBAL_RANK_ARTIST.ARTIST_ID\n" +
            "WHERE GLOBAL_RANK_ARTIST.RANK >= ?1 AND GLOBAL_RANK_ARTIST.RANK < ?2\n" +
            "ORDER BY GLOBAL_RANK_ARTIST.RANK",
        nativeQuery = true,
    )
    fun findGlobalList(
        from: Long,
        to: Long,
    ): List<Artist>

    fun findByNameLike(name: String): List<Artist>
}
