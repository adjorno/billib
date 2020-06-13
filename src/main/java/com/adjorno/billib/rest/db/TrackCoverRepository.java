package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TrackCoverRepository extends CrudRepository<TrackCover, Long> {
    List<TrackCover> findByCoverUrl(String coverUrl);

    @Query(value = "select tc from TrackCover tc where tc.trackId in ?1")
    List<TrackCover> findCoversByTrackIds(List<Long> trackIds);

    @Modifying
    @Query(value = "update TrackCover tc set tc.trackId = ?2 where tc.trackId = ?1")
    void updateTrackIds(Long from, Long to);

}
