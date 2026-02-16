package com.ifochka.billib.rest.db

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
    @JoinColumn(name = "week_id")
    var week: Week? = null,
    @OneToOne
    @JoinColumn(name = "track_id")
    var track: Track? = null,
    @OneToOne
    @JoinColumn(name = "trend_type_id")
    var type: TrendType? = null,
)
