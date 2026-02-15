package com.adjorno.billib.rest.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ArtistRelationRepository : CrudRepository<ArtistRelation, Long> {

    @Modifying
    @Query(value = "update ArtistRelation ar set ar.single = ?2 where ar.single = ?1")
    fun updateSingleArtists(duplicateArtist: Artist, originalArtist: Artist)

    @Modifying
    @Query(value = "update ArtistRelation ar set ar.band = ?2 where ar.band = ?1")
    fun updateBandArtists(duplicateArtist: Artist, originalArtist: Artist)

    @Query(value = "SELECT SINGLE_ID FROM ARTIST_RELATION WHERE BAND_ID IN (?1, ?2) GROUP BY SINGLE_ID",
            nativeQuery = true)
    fun findMergingSingleIds(bandId1: Long, bandId2: Long): List<Long>

    @Query(value = "SELECT BAND_ID FROM ARTIST_RELATION WHERE SINGLE_ID IN (?1, ?2) GROUP BY BAND_ID",
            nativeQuery = true)
    fun findMergingBandIds(singleId1: Long, singleId2: Long): List<Long>

    @Modifying
    @Query(value = "delete from ArtistRelation ar where ar.single.id = ?1 and ar.band.id = ?2")
    fun deleteBySingleIdAndBandId(singleId: Long, bandId: Long)

    fun findByBand(artist: Artist): List<ArtistRelation>

    fun findBySingle(artist: Artist): List<ArtistRelation>
}
