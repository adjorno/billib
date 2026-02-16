package com.ifochka.billib.rest

import com.ifochka.billib.rest.db.Chart
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import com.ifochka.billib.rest.db.ChartRepository
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@RestController
class ChartController {
    @Autowired
    private lateinit var chartRepository: ChartRepository

    @get:RequestMapping(value = ["/chart/all"], method = [RequestMethod.GET])
    val allCharts: Iterable<Chart>
        get() = chartRepository.findAll()
}