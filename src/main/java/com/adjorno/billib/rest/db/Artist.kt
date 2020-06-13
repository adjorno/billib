package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "ARTIST")
data class Artist(
        @Id
        @Column(name = "_id")
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long? = null,

        @Column(name = "NAME")
        var name: String? = null
) {
    fun generateDuplicateTitle(title: String): String {
        return "$name - $title"
    }
}
