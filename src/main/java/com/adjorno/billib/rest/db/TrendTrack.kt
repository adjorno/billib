package com.adjorno.billib.rest.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "TREND_TRACK")
data class TrendTrack(
        @Id
        @Column(name = "_id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @OneToOne
        @JoinColumn(name = "WEEK_ID")
        var week: Week? = null,

        @OneToOne
        @JoinColumn(name = "TRACK_ID")
        var track: Track? = null,

        @OneToOne
        @JoinColumn(name = "TYPE_ID")
        var type: TrendType? = null
)
