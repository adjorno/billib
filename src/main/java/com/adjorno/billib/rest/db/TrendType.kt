package com.adjorno.billib.rest.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "TREND_TYPE")
data class TrendType(
        @Id
        @Column(name = "_id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(name = "DESCRIPTION")
        var description: String? = null
) {
    companion object {
        const val TYPE_ALL: Long = 0
        const val TYPE_GAINERS: Long = 1
        const val TYPE_DEBUTS: Long = 2
        const val TYPE_FUTURES: Long = 3
        const val TYPE_SENIORS: Long = 4
    }
}
