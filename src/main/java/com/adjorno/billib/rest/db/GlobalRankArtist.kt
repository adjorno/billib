package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "GLOBAL_RANK_ARTIST")
data class GlobalRankArtist(
        @Id
        @Column(name = "RANK")
        @GeneratedValue(strategy = GenerationType.AUTO)
        var rank: Long? = null,

        @Column(name = "ARTIST_ID")
        var artistId: Long? = null
)
