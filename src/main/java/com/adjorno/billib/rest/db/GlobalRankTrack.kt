package com.adjorno.billib.rest.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "GLOBAL_RANK_TRACK")
data class GlobalRankTrack(
    @Id
    @Column(name = "_RANK")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var rank: Long? = null,

    @Column(name = "TRACK_ID")
    var trackId: Long? = null
)
