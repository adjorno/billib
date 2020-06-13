package com.adjorno.billib.rest.db

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "TRACK_COVER")
data class TrackCover(
        @Id
        @Column(name = "TRACK_ID")
        var trackId: Long? = null,

        @Column(name = "COVER_URL")
        var coverUrl: String? = null
)
