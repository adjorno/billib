package com.adjorno.billib.rest.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface TrendTrackRepository : CrudRepository<TrendTrack, Long> {
    @Query(value = "select tt from TrendTrack tt where tt.week = ?1")
    fun findTrendsOfTheWeek(week: Week?): List<TrendTrack>?

    @Modifying
    @Query(value = "update TrendTrack tt set tt.track = ?2 where tt.track = ?1")
    fun updateTracks(from: Track, to: Track)
}