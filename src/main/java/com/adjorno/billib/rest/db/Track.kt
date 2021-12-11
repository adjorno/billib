package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "TRACK")
data class Track(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "TITLE")
    var title: String? = null,

    @OneToOne
    @JoinColumn(name = "ARTIST_ID")
    var artist: Artist? = null
) {
    override fun toString() = "${artist?.name} - $title"
}
