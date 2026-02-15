package com.adjorno.billib.rest.db

import org.springframework.data.repository.CrudRepository

interface ChartRepository : CrudRepository<Chart, Long> {

    fun findByName(chartName: String): Chart?

}
