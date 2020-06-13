package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DuplicateArtistRepository extends CrudRepository<DuplicateArtist, String> {
    @Modifying
    @Query(value = "update DuplicateArtist da set da.artist = ?2 where da.artist = ?1")
    void updateArtists(Artist from, Artist to);

    DuplicateArtist findByDuplicateName(String name);

    List<DuplicateArtist> findByArtist(Artist artist);

    @Modifying
    @Query(value = "update DuplicateArtist da set da.duplicateName = ?2 where da = ?1")
    void rename(DuplicateArtist duplicateArtist, String optimizedName);
}
