package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "DUPLICATE_ARTIST")
data class DuplicateArtist(
        @Id
        @Column(name = "DUPLICATE_NAME")
        var duplicateName: String? = null,

        @OneToOne
        @JoinColumn(name = "ARTIST_ID")
        var artist: Artist? = null

)
