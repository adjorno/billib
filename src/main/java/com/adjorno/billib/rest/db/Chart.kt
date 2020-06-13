package com.adjorno.billib.rest.db

import javax.persistence.*

@Entity
@Table(name = "CHART")
data class Chart(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(name = "NAME")
    var name: String? = null,

    @OneToOne
    @JoinColumn(name = "JOURNAL_ID")
    var journal: Journal? = null,

    @Column(name = "LIST_SIZE")
    var listSize: Int? = null,

    @Column(name = "START_DATE")
    var startDate: String? = null
) {
    override fun toString(): String = name.toString()
}
