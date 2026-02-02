package com.adjorno.billib.rest.db

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "CHART")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Chart(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "NAME")
    var name: String? = null,

    @OneToOne
    @JoinColumn(name = "JOURNAL_ID")
    var journal: Journal? = null,

    @Column(name = "LIST_SIZE")
    var listSize: Int? = null,

    @Column(name = "START_DATE")
    var startDate: String? = null,

    @Column(name = "END_DATE")
    var endDate: String? = null
) {
    override fun toString(): String = name.toString()
}
