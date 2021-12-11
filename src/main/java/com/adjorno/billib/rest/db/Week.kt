package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "WEEK")
data class Week(
    @Id
    @Column(name = "WEEK_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "DATE")
    var date: String? = null
) {
    override fun toString() = date.toString()
}
