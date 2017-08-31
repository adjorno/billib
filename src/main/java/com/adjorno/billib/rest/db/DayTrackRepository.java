package com.adjorno.billib.rest.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Date;


public interface DayTrackRepository extends CrudRepository<DayTrack, Date> {
    @Modifying
    @Query(value = "update DayTrack dt set dt.mTrack = ?2 where dt.mTrack = ?1")
    void updateTracks(Track from, Track to);

    @Query(value = "select dt from DayTrack dt order by dt.mDay desc")
    Page<DayTrack> findLast(Pageable pageable);

}
