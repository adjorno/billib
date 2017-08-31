package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TrackCoverRepository extends CrudRepository<TrackCover, Long> {
    List<TrackCover> findBymCoverUrl(String coverUrl);

    @Query(value = "select tc from TrackCover tc where tc.mTrackId in ?1")
    List<TrackCover> findCoversByTrackIds(List<Long> trackIds);

    @Modifying
    @Query(value = "update TrackCover tc set tc.mTrackId = ?2 where tc.mTrackId = ?1")
    void updateTrackIds(Long from, Long to);

}
