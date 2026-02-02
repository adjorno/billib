package com.adjorno.billib.rest.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "ARTIST")
data class Artist(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "NAME")
    var name: String? = null,

    // Generated column for normalized search (read-only)
    @Column(name = "NAME_NORMALIZED", insertable = false, updatable = false)
    var nameNormalized: String? = null
) {
    fun generateDuplicateTitle(title: String): String {
        return "$name - $title"
    }

    override fun toString(): String = name.toString()
}
