package com.adjorno.billib.rest.db

import java.time.LocalDate
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "TRACK")
data class Track(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "TITLE")
    var title: String? = null,

    @OneToOne
    @JoinColumn(name = "ARTIST_ID")
    var artist: Artist? = null,

    // Denormalized fields for search performance
    @Column(name = "ARTIST_NAME")
    var artistName: String? = null,

    @Column(name = "FIRST_CHART_DATE")
    var firstChartDate: LocalDate? = null,

    @Column(name = "PEAK_GLOBAL_RANK")
    var peakGlobalRank: Int? = null,

    @Column(name = "TOTAL_WEEKS_ON_CHART")
    var totalWeeksOnChart: Int = 0
) {
    override fun toString() = "${artist?.name ?: artistName} - $title"
}
