package com.ifochka.m14n.rest

import com.ifochka.m14n.rest.db.Chart
import com.ifochka.m14n.rest.db.ChartRepository
import com.ifochka.m14n.rest.notification.ChartUpdateResult
import com.ifochka.m14n.rest.notification.ChartUpdateService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ChartController(
    private val chartRepository: ChartRepository,
    private val chartUpdateService: ChartUpdateService,
) {
    @RequestMapping(value = ["/chart/all"], method = [RequestMethod.GET])
    fun allCharts(): Iterable<Chart> = chartRepository.findAll()

    @RequestMapping(value = ["/chart/forceUpdate"], method = [RequestMethod.POST])
    fun forceUpdate(): ChartUpdateResult = chartUpdateService.checkForNewCharts()
}
