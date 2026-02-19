package com.ifochka.m14n.rest

import com.ifochka.m14n.rest.db.Chart
import com.ifochka.m14n.rest.db.ChartRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ChartController {
    @Autowired
    private lateinit var chartRepository: ChartRepository

    @get:RequestMapping(value = ["/chart/all"], method = [RequestMethod.GET])
    val allCharts: Iterable<Chart>
        get() = chartRepository.findAll()
}
