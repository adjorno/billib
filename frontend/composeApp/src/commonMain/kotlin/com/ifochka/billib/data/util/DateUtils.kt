package com.ifochka.billib.data.util

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
    /**
     * Find Monday of the week containing the given date.
     * If date is already Monday, returns the same date.
     */
    fun getMondayOfWeek(date: LocalDate): LocalDate {
        val dayOfWeek = date.dayOfWeek
        val daysFromMonday = when (dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
            else -> 0
        }
        return date.minus(daysFromMonday, DateTimeUnit.DAY)
    }

    /**
     * Parse "yyyy-MM-dd" string to LocalDate.
     * Returns null if parsing fails.
     */
    fun parseChartDate(dateString: String): LocalDate? =
        try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }

    /**
     * Format LocalDate to "yyyy-MM-dd" string.
     */
    fun formatChartDate(date: LocalDate): String = date.toString()

    /**
     * Get previous Monday (7 days back from current date).
     */
    fun getPreviousWeek(date: LocalDate): LocalDate = date.minus(7, DateTimeUnit.DAY)

    /**
     * Get next Monday (7 days forward from current date).
     */
    fun getNextWeek(date: LocalDate): LocalDate = date.plus(7, DateTimeUnit.DAY)

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
        val sunday = mondayDate.plus(6, DateTimeUnit.DAY)

        val startMonth = mondayDate.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        val endMonth = sunday.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)

        return if (mondayDate.month == sunday.month) {
            "$startMonth ${mondayDate.dayOfMonth}-${sunday.dayOfMonth}, ${mondayDate.year}"
        } else {
            "$startMonth ${mondayDate.dayOfMonth} - $endMonth ${sunday.dayOfMonth}, ${mondayDate.year}"
        }
    }
}
