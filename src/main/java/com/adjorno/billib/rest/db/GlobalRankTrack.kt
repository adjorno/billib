package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "GLOBAL_RANK_TRACK")
data class GlobalRankTrack(
        @Id
        @Column(name = "RANK")
        @GeneratedValue(strategy = GenerationType.AUTO)
        var rank: Long? = null,

        @Column(name = "TRACK_ID")
        var trackId: Long? = null
)
