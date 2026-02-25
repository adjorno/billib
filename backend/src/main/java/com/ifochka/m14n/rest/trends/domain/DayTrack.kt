package com.ifochka.m14n.rest.trends.domain

import com.ifochka.m14n.rest.catalog.domain.Track
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.sql.Date

@Entity
@Table(name = "DAY_TRACK")
data class DayTrack(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "day_track_seq")
    @SequenceGenerator(name = "day_track_seq", sequenceName = "day_track__id_seq", allocationSize = 1)
    @Column(name = "_id")
    var id: Long? = null,
    @OneToOne
    @JoinColumn(name = "track_id")
    var track: Track? = null,
    @Column(name = "date")
    var day: Date? = null,
)
