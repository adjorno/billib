package com.ifochka.m14n.rest.rankings.domain

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
    @Column(name = "rank")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var rank: Long? = null,
    @Column(name = "track_id")
    var trackId: Long? = null,
)
