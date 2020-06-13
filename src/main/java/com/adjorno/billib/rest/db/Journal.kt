package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "JOURNAL")
data class Journal(
        @Id
        @Column(name = "_id")
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long? = null,

        @Column(name = "NAME")
        var name: String? = null
)
