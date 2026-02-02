package com.adjorno.billib.rest.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

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
