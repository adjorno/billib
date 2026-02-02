package com.adjorno.billib.rest.db

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

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
