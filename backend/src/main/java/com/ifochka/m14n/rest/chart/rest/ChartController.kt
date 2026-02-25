package com.ifochka.m14n.rest.chart.rest

import com.ifochka.m14n.rest.chart.domain.Chart
import com.ifochka.m14n.rest.chart.domain.ChartRepository
import com.ifochka.m14n.rest.notification.ChartUpdateResult
import com.ifochka.m14n.rest.notification.ChartUpdateService
import com.ifochka.m14n.rest.notification.fcm.FcmService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ChartController(
    private val chartRepository: ChartRepository,
    private val chartUpdateService: ChartUpdateService,
    private val fcmService: FcmService,
) {
    @RequestMapping(value = ["/chart/all"], method = [RequestMethod.GET])
    fun allCharts(): Iterable<Chart> = chartRepository.findAll()

    // TODO(#75): Gate with access token before re-enabling
    // @RequestMapping(value = ["/chart/forceUpdate"], method = [RequestMethod.POST])
    fun forceUpdate(): ChartUpdateResult {
        val result = chartUpdateService.checkForNewCharts()
        val newCharts = result.newCharts
        if (newCharts.isNotEmpty()) {
            val count = newCharts.size
            fcmService.sendToTopic(
                topic = "new-chart",
                title = "New Chart${if (count > 1) "s" else ""} Available",
                body = newCharts.joinToString(", "),
            )
        }
        return result
    }
}
