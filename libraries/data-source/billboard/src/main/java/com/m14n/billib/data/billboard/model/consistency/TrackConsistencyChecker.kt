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
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Title) {
            return false
        }
        return parts.containsAll(other.parts) || other.parts.containsAll(parts)
    }

    companion object {
        fun fromRawValue(rawValue: String) = Title(
            raw = rawValue,
            parts = rawValue
                .cleanFromSpecialCharacters()
                .split(
                    " ", "'", "’", "-", "?",
                    ignoreCase = true,
                )
                .filter { it.isNotBlank() }
                .map { CleanTitlePart.fromRawValue(it) }
        )
    }
}

data class CleanTitlePart(
    val value: String
) {
    companion object {
        fun fromRawValue(rawValue: String): CleanTitlePart {
            var cleanedValue = rawValue.trim().lowercase()
            cleanedValue = cleanedValue.cleanFromSpecialWordEndings()
            cleanedValue = cleanedValue.filter { it.isLetterOrDigit() }
            cleanedValue = cleanedValue.trim().replace("  ", " ").lowercase()
            return CleanTitlePart(cleanedValue)
        }
    }
}

data class Artist(
    val raw: String,
    val parts: Set<CleanArtistPart>,
) {
    override fun hashCode() = parts.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Artist) {
            return false
        }
        return parts.containsAll(other.parts) || other.parts.containsAll(parts)
    }

    companion object {
        fun fromRawValue(rawValue: String) = Artist(
            raw = rawValue,
            parts = rawValue.cleanFromSpecialCharacters()
                .split(
                    ",", "/", " duet with ", " with ", " and ", " or ", " & ",
                    " ft ", " feat ", " featuring ", " + ", " Conducted By ",
                    ignoreCase = true
                ).map {
                    it.split(
                        " x ",
                        ignoreCase = true
                    )
                }
                .flatten()
                .map { CleanArtistPart.fromRawValue(it) }.toSet()
        )
    }
}

data class CleanArtistPart(
    val value: String
) {
    companion object {
        val specialCharsToIgnore = setOf('\"', '.')

        val optionalPrefixes = listOf(
            "chorus of ",
            "the ",
            "dj ",
        )

        val sameArtistNames = listOf(
            "chris deburgh" to "chris de burgh",
            "dennis de young" to "dennis deyoung",
            "kanye west" to "ye",
            "kaliii" to "kali",
            "snoop doggy dogg" to "snoop dogg",
            "product g&b" to "product g",
            "p!nk" to "pink",
            "shawnna" to "shawna",
            "KeKe Wyatt" to "Ketara Wyatt",
            "Musiq Soulchild" to "Musiq",
            "QB s Finest" to "QB Finest",
            "Braveheart s" to "Bravehearts",
            "D 12" to "D12",
            "Ayana Of Aaries" to "Ayana",
            "Tammy Ruggieri" to "Tammy Ruggeri",
            "Bonecrusher" to "Bone Crusher",
            "Mason Betha" to "Mase",
            "Maroon 5" to "Maroon5",
            "Pretty Ricky" to "Pretty Rickie",
            "Panic! At The Disco" to "Panic At The Disco",
            "Plies" to "Pli",
            "Bobby Valentino" to "Bobby V"
        )

        fun fromRawValue(rawValue: String): CleanArtistPart {
            var cleanedValue = rawValue.trim().lowercase()
            cleanedValue = cleanedValue.filterNot { specialCharsToIgnore.contains(it) }
            optionalPrefixes.forEach { optionalPrefix ->
                if (cleanedValue.startsWith(optionalPrefix, ignoreCase = true)) {
                    cleanedValue = cleanedValue.substring(optionalPrefix.length)
                }
            }
            sameArtistNames.forEach {
                cleanedValue = cleanedValue.replace(it.first, it.second, ignoreCase = true)
            }
            cleanedValue = cleanedValue.trim().replace("  ", " ").lowercase()
            return CleanArtistPart(cleanedValue)
        }
    }
}


fun String.cleanFromSpecialWordEndings() = specialWordEndingsReplacements.fold(this) { text, wordEndingReplacement ->
    if (text.endsWith(wordEndingReplacement.first, ignoreCase = true)) {
        text.substring(0, text.length - wordEndingReplacement.first.length) + wordEndingReplacement.second
    } else {
        text
    }

}

fun String.cleanFromSpecialCharacters() = specialCharsReplacements.fold(this) { text, charReplacement ->
    text.replace(charReplacement.first, charReplacement.second)
}

val specialWordEndingsReplacements = listOf(
    "ing" to "in",
    "&" to "and",
    "oooh" to "ooh",
    "a**" to "ass",
)

val specialCharsReplacements = listOf(
    '(' to ' ',
    ')' to ' ',
    '-' to ' ',
    '\'' to ' ',
    '"' to ' ',
    '’' to ' ',
    '.' to ' ',
    '$' to 's',
    'é' to 'e',
)