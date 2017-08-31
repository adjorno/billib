package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DuplicateArtistRepository extends CrudRepository<DuplicateArtist, String> {
    @Modifying
    @Query(value = "update DuplicateArtist da set da.mArtist = ?2 where da.mArtist = ?1")
    void updateArtists(Artist from, Artist to);

    DuplicateArtist findBymDuplicateName(String name);

    List<DuplicateArtist> findBymArtist(Artist artist);

    @Modifying
    @Query(value = "update DuplicateArtist da set da.mDuplicateName = ?2 where da = ?1")
    void rename(DuplicateArtist duplicateArtist, String optimizedName);
}
