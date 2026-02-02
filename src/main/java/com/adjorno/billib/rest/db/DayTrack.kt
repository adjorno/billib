package com.adjorno.billib.rest.db

import java.sql.Date
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "DAY_TRACK")
data class DayTrack(
        @Id
        @Column(name = "_DAY")
        var day: Date? = null,

        @OneToOne
        @JoinColumn(name = "TRACK_ID")
        var track: Track? = null,

        @Column(name = "DESCRIPTION")
        var desc: String? = null
)
