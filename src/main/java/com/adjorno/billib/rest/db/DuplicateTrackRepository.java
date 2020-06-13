package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DuplicateTrackRepository extends CrudRepository<DuplicateTrack, String> {
    @Modifying
    @Query(value = "update DuplicateTrack dt set dt.track = ?2 where dt.track = ?1")
    void updateTracks(Track from, Track to);

    DuplicateTrack findByDuplicateTitle(String trackTitle);

    @Modifying
    @Query(value = "update DuplicateTrack dt set dt.duplicateTitle = ?2 where dt = ?1")
    void rename(DuplicateTrack duplicateTrack, String optimizedTitle);
}
