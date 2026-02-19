package com.ifochka.m14n.data.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Get current date. Platform-specific implementation due to Clock.System
 * not being accessible in commonMain metadata compilation.
 */
internal expect fun currentDate(): LocalDate

/**
 * Utilities for date calculations and formatting for chart week navigation.
 * All dates are in "yyyy-MM-dd" format (ISO-8601).
 */
object DateUtils {
    private const val DAYS_IN_WEEK = 7
    private const val DAYS_TO_SUNDAY = 6
    private const val MONTH_ABBREVIATION_LENGTH = 3
    private const val DAYS_FROM_MONDAY_TO_TUESDAY = 1
    private const val DAYS_FROM_MONDAY_TO_WEDNESDAY = 2
    private const val DAYS_FROM_MONDAY_TO_THURSDAY = 3
    private const val DAYS_FROM_MONDAY_TO_FRIDAY = 4
    private const val DAYS_FROM_MONDAY_TO_SATURDAY = 5
    private const val DAYS_FROM_MONDAY_TO_SUNDAY = 6

    /**
     * Find Monday of the week containing the given date.
     * If date is already Monday, returns the same date.
     */
    fun getMondayOfWeek(date: LocalDate): LocalDate {
        val dayOfWeek = date.dayOfWeek
        val daysFromMonday = when (dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> DAYS_FROM_MONDAY_TO_TUESDAY
            DayOfWeek.WEDNESDAY -> DAYS_FROM_MONDAY_TO_WEDNESDAY
            DayOfWeek.THURSDAY -> DAYS_FROM_MONDAY_TO_THURSDAY
            DayOfWeek.FRIDAY -> DAYS_FROM_MONDAY_TO_FRIDAY
            DayOfWeek.SATURDAY -> DAYS_FROM_MONDAY_TO_SATURDAY
            DayOfWeek.SUNDAY -> DAYS_FROM_MONDAY_TO_SUNDAY
        }
        return date.minus(daysFromMonday, DateTimeUnit.DAY)
    }

    /**
     * Parse "yyyy-MM-dd" string to LocalDate.
     * Returns null if parsing fails.
     */
    @Suppress("SwallowedException")
    fun parseChartDate(dateString: String): LocalDate? =
        try {
            LocalDate.parse(dateString)
        } catch (e: IllegalArgumentException) {
            // Invalid date format - return null as expected behavior
            null
        }

    /**
     * Format LocalDate to "yyyy-MM-dd" string.
     */
    fun formatChartDate(date: LocalDate): String = date.toString()

    /**
     * Get previous Monday (7 days back from current date).
     */
    fun getPreviousWeek(date: LocalDate): LocalDate = date.minus(DAYS_IN_WEEK, DateTimeUnit.DAY)

    /**
     * Get next Monday (7 days forward from current date).
     */
    fun getNextWeek(date: LocalDate): LocalDate = date.plus(DAYS_IN_WEEK, DateTimeUnit.DAY)

    /**
     * Validate date is within allowed range.
     * @param date Date to validate
     * @param startDate Earliest allowed date (e.g., chart start date)
     * @param endDate Latest allowed date (defaults to today if null)
     * @return true if date is within [startDate, endDate]
     */
    fun isDateInRange(
        date: LocalDate,
        startDate: String?,
        endDate: String? = null,
    ): Boolean {
        val start = startDate?.let { parseChartDate(it) } ?: return false
        val end = endDate?.let { parseChartDate(it) } ?: getToday()

        return date in start..end
    }

    /**
     * Get today's date in LocalDate.
     */
    fun getToday(): LocalDate = currentDate()

    /**
     * Get today's date in "yyyy-MM-dd" format.
     */
    fun getTodayAsString(): String = formatChartDate(getToday())

    /**
     * Format week range for display (e.g., "Feb 12 - Feb 18, 2024").
     * @param mondayDate The Monday of the week to format
     * @return Formatted week range string
     */
    fun formatWeekRange(mondayDate: LocalDate): String {
        val sunday = mondayDate.plus(DAYS_TO_SUNDAY, DateTimeUnit.DAY)

        val startMonth = mondayDate.month.name.lowercase()
            .replaceFirstChar { it.uppercase() }
            .take(MONTH_ABBREVIATION_LENGTH)
        val endMonth = sunday.month.name.lowercase()
            .replaceFirstChar { it.uppercase() }
            .take(MONTH_ABBREVIATION_LENGTH)

        return if (mondayDate.month == sunday.month) {
            "$startMonth ${mondayDate.day}-${sunday.day}, ${mondayDate.year}"
        } else {
            "$startMonth ${mondayDate.day} - $endMonth ${sunday.day}, ${mondayDate.year}"
        }
    }
}
