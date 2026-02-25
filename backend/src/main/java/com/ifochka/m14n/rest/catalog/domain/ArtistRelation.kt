package com.ifochka.m14n.rest.catalog.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "ARTIST_RELATION")
data class ArtistRelation(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @OneToOne
    @JoinColumn(name = "collaboration_artist_id")
    var collaborationArtist: Artist? = null,
    @OneToOne
    @JoinColumn(name = "member_artist_id")
    var memberArtist: Artist? = null,
    @OneToOne
    @JoinColumn(name = "track_id")
    var track: Track? = null,
) {
    constructor(collaborationArtist: Artist?, memberArtist: Artist?, track: Track?) :
        this(null, collaborationArtist, memberArtist, track)
}
