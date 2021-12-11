package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "ARTIST_RELATION")
data class ArtistRelation(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "SINGLE_ID")
    var single: Artist? = null,

    @OneToOne
    @JoinColumn(name = "BAND_ID")
    var band: Artist? = null
) {
    constructor(single: Artist?, band: Artist?) : this(null, single, band)
}
