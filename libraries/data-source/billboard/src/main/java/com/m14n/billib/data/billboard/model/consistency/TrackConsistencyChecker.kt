package com.m14n.billib.data.billboard.model.consistency

import com.m14n.billib.data.billboard.model.BBTrack

fun interface TrackConsistencyChecker {
    fun check(actualTrack: BBTrack, previousChartTrack: BBTrack): Result

    data class Result(val inconsistencies: List<Inconsistency>) {
        val unacceptable = inconsistencies.any { trackInconsistency -> !trackInconsistency.acceptable }
    }

    sealed interface Inconsistency {
        val acceptable: Boolean

        data class Artist(val actual: String, val expected: String, override val acceptable: Boolean = false) :
            Inconsistency

        data class Title(val actual: String, val expected: String, override val acceptable: Boolean = false) :
            Inconsistency
    }
}

val legacyTrackConsistencyChecker = TrackConsistencyChecker { actualTrack, previousChartTrack ->
    val actualTitle = Title.fromRawValue(actualTrack.title)
    val expectedTitle = Title.fromRawValue(previousChartTrack.title)
    val actualArtist = Artist.fromRawValue(actualTrack.artist)
    val expectedArtist = Artist.fromRawValue(previousChartTrack.artist)
    return@TrackConsistencyChecker TrackConsistencyChecker.Result(
        inconsistencies = listOfNotNull(
            if (actualTitle.raw == expectedTitle.raw) {
                null
            } else {
                TrackConsistencyChecker.Inconsistency.Title(
                    actual = actualTrack.title,
                    expected = previousChartTrack.title,
                    acceptable = actualTitle == expectedTitle,
                )
            },
            if (actualArtist.raw == expectedArtist.raw) {
                null
            } else TrackConsistencyChecker.Inconsistency.Artist(
                actual = actualTrack.artist,
                expected = previousChartTrack.artist,
                acceptable = actualArtist == expectedArtist
            ),
        )
    )
}

data class Title(
    val raw: String,
    val parts: List<CleanTitlePart>,
) {
    override fun hashCode() = parts.hashCode()
    override fun equals(other: Any?) = parts == (other as? Title)?.parts

    companion object {
        fun fromRawValue(rawValue: String) = Title(
            raw = rawValue,
            parts = rawValue.split("\\s+".toRegex()).map { CleanTitlePart.fromRawValue(it) }
        )
    }
}

data class CleanTitlePart(
    val value: String
) {
    companion object {
        fun fromRawValue(rawValue: String): CleanTitlePart {
            var cleanedValue = rawValue.trim().lowercase()
            if ((cleanedValue.first() == '"') && (cleanedValue.last() == '"')) {
                cleanedValue = cleanedValue.substring(1, cleanedValue.length - 1)
            }
            return CleanTitlePart(cleanedValue)
        }
    }
}

data class Artist(
    val raw: String,
    val parts: List<CleanArtistPart>,
) {
    override fun hashCode() = parts.hashCode()
    override fun equals(other: Any?) = parts == (other as? Artist)?.parts

    companion object {
        fun fromRawValue(rawValue: String) = Artist(
            raw = rawValue,
            parts = rawValue.split(",".toRegex()).map { CleanArtistPart.fromRawValue(it) }
        )
    }
}

data class CleanArtistPart(
    val value: String
) {
    companion object {
        val specialCharsToIgnore = setOf('\"')

        fun fromRawValue(rawValue: String): CleanArtistPart {
            val cleanedValue = rawValue.trim().lowercase().filterNot { specialCharsToIgnore.contains(it) }
            return CleanArtistPart(cleanedValue)
        }
    }
}