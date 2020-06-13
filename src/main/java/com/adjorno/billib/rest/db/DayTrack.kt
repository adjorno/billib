package com.adjorno.billib.rest.db

import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "DAY_TRACK")
data class DayTrack(
        @Id
        @Column(name = "DAY")
        var day: Date? = null,

        @OneToOne
        @JoinColumn(name = "TRACK_ID")
        var track: Track? = null,

        @Column(name = "DESCRIPTION")
        var desc: String? = null
)
