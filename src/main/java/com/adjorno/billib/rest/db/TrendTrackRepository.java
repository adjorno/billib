package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TrendTrackRepository extends CrudRepository<TrendTrack, Long> {

    @Query(value = "select tt from TrendTrack tt where tt.week = ?1")
    List<TrendTrack> findTrendsOfTheWeek(Week week);

    @Modifying
    @Query(value = "update TrendTrack tt set tt.track = ?2 where tt.track = ?1")
    void updateTracks(Track from, Track to);
}
