package com.ifochka.billib.rest.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "GLOBAL_RANK_ARTIST")
data class GlobalRankArtist(
    @Id
    @Column(name = "rank")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var rank: Long? = null,
    @Column(name = "artist_id")
    var artistId: Long? = null,
)
