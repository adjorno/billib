package com.ifochka.m14n.rest.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "GLOBAL_RANK_ARTIST")
data class GlobalRankArtistView(
    @Id
    @Column(name = "ARTIST_ID")
    var artistId: Long,
    @Column(name = "RANK")
    var rank: Long,
    @Column(name = "UNIQUE_TRACKS")
    var uniqueTracks: Long,
    @Column(name = "TOTAL_APPEARANCES")
    var totalAppearances: Long,
    @Column(name = "PEAK_POSITION")
    var peakPosition: Int,
) {
    override fun toString() =
        "Artist #$artistId: Global Rank #$rank ($uniqueTracks tracks, $totalAppearances appearances)"
}
