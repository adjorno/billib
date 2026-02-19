package com.ifochka.m14n.rest.notification

import com.ifochka.m14n.rest.db.DayTrackRepository
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
) {
    private val logger = LoggerFactory.getLogger(NotificationScheduler::class.java)

    @Scheduled(cron = "0 0 9 * * *", zone = "UTC")
    fun sendTrackOfDayNotification() {
        val today = Date.valueOf(LocalDate.now())
        val dayTrack = dayTrackRepository.findById(today).orElse(null) ?: run {
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
        val newCharts = chartUpdateService.checkForNewCharts()
        if (newCharts.isEmpty()) return
        val count = newCharts.size
        fcmService.sendToTopic(
            topic = "new-chart",
            title = "New Chart${if (count > 1) "s" else ""} Available",
            body = newCharts.joinToString(", "),
        )
    }
}
