package com.m14n.billib.data.billboard.model.consistency

import com.m14n.billib.data.billboard.BB
import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBTrack

fun interface ChartConsistencyChecker {
    fun check(chart: BBChart, previousChart: BBChart): Result

    data class Result(val inconsistencies: Map<BBTrack, List<TrackConsistencyChecker.Inconsistency>>) {

        val unacceptable = inconsistencies.any {
            trackInconsistencies -> trackInconsistencies.value.any { trackInconsistency -> !trackInconsistency.acceptable }
        }

        override fun toString() = """
${if (unacceptable) "FAILURE" else "SUCCESS"}:
${inconsistencies.toList().joinToString(separator = "\n") }
        """.trimIndent()
    }
}



val legacyChartConsistencyChecker = ChartConsistencyChecker { chart, previousChart ->
    return@ChartConsistencyChecker ChartConsistencyChecker.Result(
        inconsistencies = chart.tracks
            .mapNotNull { track ->
                val theLastWeek = BB.extractLastWeekRank(track.positionInfo?.lastWeek ?: "--")
                if (theLastWeek > 0 && theLastWeek <= previousChart.tracks.size) {
                    val potentialPreviousTracks = previousChart.tracks.filter { it.rank == theLastWeek }
                    val potentialInconsistencies = potentialPreviousTracks.map { legacyTrackConsistencyChecker.check(track, it) }
                    if (potentialInconsistencies.size > 1) {
                        println("OLOLO")
                    }
                    if (potentialInconsistencies.any { it.inconsistencies.isEmpty() }) {
                        null
                    } else {
                        val trackInconsistency =
                        potentialInconsistencies.firstOrNull { !it.unacceptable }?.inconsistencies
                        ?: potentialInconsistencies.last().inconsistencies
                        track to trackInconsistency
                    }
                } else {
                    null
                }
            }
            .toMap()
    )
}

