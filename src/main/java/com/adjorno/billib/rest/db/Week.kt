package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "WEEK")
data class Week(
        @Id
        @Column(name = "WEEK_ID")
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long? = null,

        @Column(name = "DATE")
        var date: String? = null
)
