package com.adjorno.billib.rest.db

import java.time.LocalDate
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "CHART_TRACK_POSITION")
data class ChartTrackPosition(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "TRACK_ID")
    var trackId: Long,

    @Column(name = "CHART_LIST_ID")
    var chartListId: Long,

    @Column(name = "_RANK")
    var rank: Int,

    @Column(name = "LAST_WEEK_RANK")
    var lastWeekRank: Int = 0,

    // Denormalized dimensions for query performance
    @Column(name = "WEEK_DATE")
    var weekDate: LocalDate,

    @Column(name = "CHART_ID")
    var chartId: Long,

    @Column(name = "ARTIST_ID")
    var artistId: Long,

    @Column(name = "TRACK_TITLE")
    var trackTitle: String,

    @Column(name = "ARTIST_NAME")
    var artistName: String,

    // Generated columns (read-only)
    @Column(name = "RANK_CHANGE", insertable = false, updatable = false)
    var rankChange: Int? = null,

    @Column(name = "IS_DEBUT", insertable = false, updatable = false)
    var isDebut: Boolean = false,

    // Transient fields for relationships (optional - load separately if needed)
    @Transient
    var track: Track? = null,

    @Transient
    var artist: Artist? = null
) {
    override fun toString() = "$artistName - $trackTitle (#$rank on $weekDate)"
}
