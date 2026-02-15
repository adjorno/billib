package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ArtistRelationRepository extends CrudRepository<ArtistRelation, Long> {

    @Modifying
    @Query(value = "update ArtistRelation ar set ar.single = ?2 where ar.single = ?1")
    void updateSingleArtists(Artist duplicateArtist, Artist originalArtist);

    @Modifying
    @Query(value = "update ArtistRelation ar set ar.band = ?2 where ar.band = ?1")
    void updateBandArtists(Artist duplicateArtist, Artist originalArtist);

    @Query(value = "SELECT SINGLE_ID FROM ARTIST_RELATION WHERE BAND_ID IN (?1, ?2) GROUP BY SINGLE_ID",
            nativeQuery = true)
    List<Long> findMergingSingleIds(Long bandId1, Long bandId2);

    @Query(value = "SELECT BAND_ID FROM ARTIST_RELATION WHERE SINGLE_ID IN (?1, ?2) GROUP BY BAND_ID",
            nativeQuery = true)
    List<Long> findMergingBandIds(Long singleId1, Long singleId2);

    @Modifying
    @Query(value = "delete from ArtistRelation ar where ar.single.id = ?1 and ar.band.id = ?2")
    void deleteBySingleIdAndBandId(Long singleId, Long bandId);

    List<ArtistRelation> findByBand(Artist artist);

    List<ArtistRelation> findBySingle(Artist artist);
}
