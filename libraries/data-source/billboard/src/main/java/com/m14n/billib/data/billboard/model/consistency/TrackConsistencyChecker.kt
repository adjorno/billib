package com.m14n.billib.data.billboard.model.consistency

import com.m14n.billib.data.billboard.model.BBTrack

fun interface TrackConsistencyChecker {
    fun check(actualTrack: BBTrack, previousChartTrack: BBTrack): Result

    data class Result(val inconsistencies: List<Inconsistency>) {
        val unacceptable = inconsistencies.any { trackInconsistency -> !trackInconsistency.acceptable }
    }

    sealed class Inconsistency(val acceptable: Boolean) {
        data class Artist(val actual: String, val expected: String) : Inconsistency(false)
        data class Title(val actual: String, val expected: String) : Inconsistency(false)
    }
}

val legacyTrackConsistencyChecker = TrackConsistencyChecker { actualTrack, previousChartTrack ->
    return@TrackConsistencyChecker TrackConsistencyChecker.Result(
        inconsistencies = listOfNotNull(
            if (actualTrack.title.equals(previousChartTrack.title, ignoreCase = true)) {
                null
            } else TrackConsistencyChecker.Inconsistency.Title(
                actual = actualTrack.title,
                expected = previousChartTrack.title
            ),
            if (actualTrack.artist.equals(previousChartTrack.artist, ignoreCase = true)) {
                null
            } else TrackConsistencyChecker.Inconsistency.Artist(
                actual = actualTrack.artist,
                expected = previousChartTrack.artist
            ),
        )
    )
}
