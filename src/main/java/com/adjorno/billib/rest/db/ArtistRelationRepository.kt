package com.adjorno.billib.rest.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ArtistRelationRepository : CrudRepository<ArtistRelation, Long> {

    @Modifying
    @Query(value = "update ArtistRelation ar set ar.artist1 = ?2 where ar.artist1 = ?1")
    fun updateArtist1(duplicateArtist: Artist, originalArtist: Artist)

    @Modifying
    @Query(value = "update ArtistRelation ar set ar.artist2 = ?2 where ar.artist2 = ?1")
    fun updateArtist2(duplicateArtist: Artist, originalArtist: Artist)

    @Query(value = "SELECT artist_id_1 FROM artist_relation WHERE artist_id_2 IN (?1, ?2) GROUP BY artist_id_1",
            nativeQuery = true)
    fun findMergingArtist1Ids(artistId1: Long, artistId2: Long): List<Long>

    @Query(value = "SELECT artist_id_2 FROM artist_relation WHERE artist_id_1 IN (?1, ?2) GROUP BY artist_id_2",
            nativeQuery = true)
    fun findMergingArtist2Ids(artistId1: Long, artistId2: Long): List<Long>

    @Modifying
    @Query(value = "delete from ArtistRelation ar where ar.artist1.id = ?1 and ar.artist2.id = ?2")
    fun deleteByArtist1IdAndArtist2Id(artist1Id: Long, artist2Id: Long)

    fun findByArtist1(artist: Artist): List<ArtistRelation>

    fun findByArtist2(artist: Artist): List<ArtistRelation>

    // Find all relations where the artist appears as either artist1 or artist2
    @Query(value = "select ar from ArtistRelation ar where ar.artist1 = ?1 or ar.artist2 = ?1")
    fun findByArtist(artist: Artist): List<ArtistRelation>
}
