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
    @JoinColumn(name = "artist_id_1")
    var artist1: Artist? = null,

    @OneToOne
    @JoinColumn(name = "artist_id_2")
    var artist2: Artist? = null,

    @OneToOne
    @JoinColumn(name = "track_id")
    var track: Track? = null
) {
    constructor(artist1: Artist?, artist2: Artist?, track: Track?) : this(null, artist1, artist2, track)
}
