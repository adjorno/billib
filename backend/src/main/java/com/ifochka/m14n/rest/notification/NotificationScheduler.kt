package com.ifochka.m14n.rest.notification

import com.ifochka.m14n.rest.notification.fcm.FcmService
import com.ifochka.m14n.rest.trends.domain.DayTrackRepository
import com.ifochka.m14n.rest.trends.domain.DayTrackService
import com.ifochka.m14n.rest.trends.domain.TrendType
import com.ifochka.m14n.rest.trends.domain.TrendsService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate

@Service
class NotificationScheduler(
    private val fcmService: FcmService,
    private val dayTrackRepository: DayTrackRepository,
    private val chartUpdateService: ChartUpdateService,
    private val trendsService: TrendsService,
    private val dayTrackService: DayTrackService,
) {
    private val logger = LoggerFactory.getLogger(NotificationScheduler::class.java)

    @Scheduled(cron = "0 0 9 * * *", zone = "UTC")
    fun sendTrackOfDayNotification() {
        val today = Date.valueOf(LocalDate.now())
        val dayTrack = dayTrackRepository.findByDay(today) ?: run {
            logger.info("No track of the day set for $today, skipping notification")
            return
        }
        val track = dayTrack.track ?: run {
            logger.warn("Day track for $today has no track reference, skipping notification")
            return
        }
        val body = "${track.artist?.name ?: track.artistName} — ${track.title}"
        fcmService.sendToTopic(
            topic = "track-of-day",
            title = "Track of the Day",
            body = body,
        )
    }

    @Scheduled(cron = "0 0 */3 * * *", zone = "UTC")
    fun checkAndNotifyNewCharts() {
        val result = chartUpdateService.checkForNewCharts()
        val newCharts = result.newCharts
        if (newCharts.isEmpty()) return

        val hot100Week = result.newWeeks["Hot 100"]
        if (hot100Week != null) {
            logger.info("Hot 100 imported for week $hot100Week — generating trends and filling DayTrack")
            trendsService.generateTrends(hot100Week, TrendType.TYPE_ALL)
            dayTrackService.fillNext14Days()
        }

        val count = newCharts.size
        fcmService.sendToTopic(
            topic = "new-chart",
            title = "New Chart${if (count > 1) "s" else ""} Available",
            body = newCharts.joinToString(", "),
        )
    }
}
