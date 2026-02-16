package com.ifochka.billib.rest

import org.springframework.data.util.Pair
import java.util.*

class UpdateResult {
    companion object {
        const val RESULT_OK = 0
        const val RESULT_DIFFERENT_SIZE = 1
        const val RESULT_DUPLICATE = 2
        const val RESULT_FAILED = 3

        fun toString(result: Int): String {
            return when (result) {
                RESULT_OK -> "OK"
                RESULT_DIFFERENT_SIZE -> "DIFFERENT_SIZE"
                RESULT_DUPLICATE -> "DUPLICATE"
                RESULT_FAILED -> "FAILED"
                else -> "UNEXPECTED RESULT"
            }
        }
    }

    private val chartUpdates: MutableList<Pair<String, Int>> = mutableListOf()

    var updateWeek: Date? = null

    fun addChartUpdate(chart: String, result: Int) {
        chartUpdates.add(Pair.of(chart, result))
    }

    fun getChartUpdates(): List<Pair<String, Int>> {
        return chartUpdates
    }
}
