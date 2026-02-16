package com.ifochka.billib.rest.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface DuplicateTrackRepository : CrudRepository<DuplicateTrack, String> {
    @Modifying
    @Query(value = "update DuplicateTrack dt set dt.track = ?2 where dt.track = ?1")
    fun updateTracks(
        from: Track,
        to: Track,
    )

    fun findByDuplicateTitle(trackTitle: String): DuplicateTrack?

    @Modifying
    @Query(value = "update DuplicateTrack dt set dt.duplicateTitle = ?2 where dt = ?1")
    fun rename(
        duplicateTrack: DuplicateTrack,
        optimizedTitle: String,
    )
}
