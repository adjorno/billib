package com.ifochka.m14n.ui.bestsongs

import com.ifochka.m14n.data.util.DateUtils
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

sealed interface DateRange {
    data object AllTime : DateRange

    data class LastPeriod(
        val years: Int = 0,
        val months: Int = 0,
        val days: Int = 0,
    ) : DateRange

    data class SinceForDuration(
        val fromDate: String,
        val durationYears: Int = 0,
        val durationMonths: Int = 0,
    ) : DateRange

    data class CustomRange(
        val from: String?,
        val to: String?,
    ) : DateRange
}

fun DateRange.toFromTo(): Pair<String?, String?> {
    val today = DateUtils.getToday()
    return when (this) {
        is DateRange.AllTime -> null to null
        is DateRange.LastPeriod ->
            today.minus(DatePeriod(years, months, days)).toString() to today.toString()
        is DateRange.SinceForDuration -> {
            val from = LocalDate.parse(fromDate)
            fromDate to from.plus(DatePeriod(durationYears, durationMonths)).toString()
        }
        is DateRange.CustomRange -> from to to
    }
}
