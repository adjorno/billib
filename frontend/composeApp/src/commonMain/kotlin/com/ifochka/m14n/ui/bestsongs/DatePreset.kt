package com.ifochka.m14n.ui.bestsongs

import com.ifochka.m14n.data.util.DateUtils
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

sealed interface DatePreset {
    data object AllTime : DatePreset

    data object Last3Months : DatePreset

    data object LastYear : DatePreset

    data class Decade(
        val startYear: Int,
    ) : DatePreset

    data class Custom(
        val from: String?,
        val to: String?,
    ) : DatePreset
}

private const val MONTHS_IN_QUARTER = 3
private const val YEARS_IN_DECADE_OFFSET = 9

fun DatePreset.toFromTo(): Pair<String?, String?> {
    val today = DateUtils.getToday()
    return when (this) {
        is DatePreset.AllTime -> null to null
        is DatePreset.Last3Months ->
            DateUtils.formatChartDate(today.minus(MONTHS_IN_QUARTER, DateTimeUnit.MONTH)) to
                DateUtils.formatChartDate(today)
        is DatePreset.LastYear ->
            DateUtils.formatChartDate(today.minus(1, DateTimeUnit.YEAR)) to
                DateUtils.formatChartDate(today)
        is DatePreset.Decade ->
            "$startYear-01-01" to "${startYear + YEARS_IN_DECADE_OFFSET}-12-31"
        is DatePreset.Custom -> from to to
    }
}
