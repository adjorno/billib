package com.m14n.billib.data.billboard

import com.m14n.billib.data.billboard.BB.CHART_DATE_FORMAT
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object BB {
    const val CHART_DATE_FORMAT_STRING = "yyyy-MM-dd"

    val CHART_DATE_FORMAT: DateFormat = SimpleDateFormat(CHART_DATE_FORMAT_STRING)
    const val OLD_LAST_WEEK_NEWBIE = "--"
    const val LAST_WEEK_NEWBIE = "-"

    fun extractLastWeekRank(lastWeekStr: String): Int {
        if (LAST_WEEK_NEWBIE != lastWeekStr && OLD_LAST_WEEK_NEWBIE != lastWeekStr) {
            try {
                return Integer.valueOf(lastWeekStr)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

        }
        return 0
    }
}

fun String.toChartDate(): Date = CHART_DATE_FORMAT.parse(this)