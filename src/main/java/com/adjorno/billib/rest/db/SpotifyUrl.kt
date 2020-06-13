package com.adjorno.billib.rest.db

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "SPOTIFY_URL")
data class SpotifyUrl(
        @Id
        @Column(name = "TRACK_ID")
        var trackId: Long? = null,

        @Column(name = "SPOTIFY_URL")
        var spotifyUrl: String? = null
)
