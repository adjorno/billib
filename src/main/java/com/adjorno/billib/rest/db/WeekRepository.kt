package com.adjorno.billib.rest.db

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface WeekRepository : CrudRepository<Week, Long> {
    @Query(value = "select w from Week w order by w.date desc")
    fun findLastWeek(pageable: Pageable): Page<Week>?

    fun findByDate(chartDate: String): Week?

    @Query(value = "select w from Week w where w.date >= ?1 order by w.date asc")
    fun findClosest(chartDate: String): List<Week>?
}