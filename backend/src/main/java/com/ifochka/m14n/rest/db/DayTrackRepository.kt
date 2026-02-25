package com.ifochka.m14n.rest.db

import com.ifochka.m14n.rest.catalog.domain.Track
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.sql.Date

interface DayTrackRepository : CrudRepository<DayTrack, Long> {
    @Modifying
    @Query(value = "update DayTrack dt set dt.track = ?2 where dt.track = ?1")
    fun updateTracks(
        from: Track,
        to: Track,
    )

    @Query(value = "select dt from DayTrack dt order by dt.day desc")
    fun findLast(pageable: Pageable): Page<DayTrack>

    fun findByDay(day: Date): DayTrack?

    fun findByDayBetween(
        from: Date,
        to: Date,
    ): List<DayTrack>
}
