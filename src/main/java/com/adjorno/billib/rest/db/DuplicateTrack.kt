package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "DUPLICATE_TRACK")
data class DuplicateTrack(
        @Id
        @Column(name = "DUPLICATE_TITLE")
        var duplicateTitle: String? = null,

        @OneToOne
        @JoinColumn(name = "TRACK_ID")
        var track: Track? = null
)