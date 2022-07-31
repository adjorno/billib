package com.adjorno.billib.rest.db

import org.springframework.data.repository.CrudRepository

interface JournalRepository : CrudRepository<Journal, Long> {
    fun findByName(name: String): Journal?
}