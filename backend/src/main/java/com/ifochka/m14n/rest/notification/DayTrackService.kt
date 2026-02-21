package com.ifochka.m14n.rest.notification

import com.ifochka.m14n.rest.db.DayTrack
import com.ifochka.m14n.rest.db.DayTrackRepository
import com.ifochka.m14n.rest.db.TrackRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Date
import java.time.LocalDate

@Service
class DayTrackService(
    private val dayTrackRepository: DayTrackRepository,
    private val trackRepository: TrackRepository,
) {
    private val logger = LoggerFactory.getLogger(DayTrackService::class.java)

    @Transactional
    fun fillNext14Days() {
        val today = LocalDate.now()
        val existing = dayTrackRepository
            .findByDayBetween(
                from = Date.valueOf(today),
                to = Date.valueOf(today.plusDays(13)),
            )
            .mapNotNull { it.day }
            .toSet()

        for (offset in 0L..13L) {
            val date = today.plusDays(offset)
            val sqlDate = Date.valueOf(date)
            if (sqlDate in existing) {
                logger.info("DayTrack already set for $date, skipping")
                continue
            }
            val monthDay = date.toString().substring(5) // MM-dd
            val trackIds = trackRepository.findDebutsOfTheDay(monthDay)
            val track = trackRepository.sortByGlobalRank(trackIds, 1).firstOrNull() ?: run {
                logger.warn("No debut track found for $date (month-day: $monthDay), skipping")
                continue
            }
            dayTrackRepository.save(DayTrack(day = sqlDate, track = track))
            logger.info("Auto-filled DayTrack for $date: ${track.artistName} — ${track.title}")
        }
    }
}
