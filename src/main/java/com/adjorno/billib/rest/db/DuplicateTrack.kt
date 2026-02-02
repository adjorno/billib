package com.adjorno.billib.rest.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

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