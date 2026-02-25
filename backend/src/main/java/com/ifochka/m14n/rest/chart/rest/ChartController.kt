package com.ifochka.m14n.rest.chart.rest

import com.ifochka.m14n.rest.chart.domain.Chart
import com.ifochka.m14n.rest.chart.domain.ChartRepository
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ChartController(
    private val chartRepository: ChartRepository,
) {
    @RequestMapping(value = ["/chart/all"], method = [RequestMethod.GET])
    fun allCharts(): Iterable<Chart> = chartRepository.findAll()
}
