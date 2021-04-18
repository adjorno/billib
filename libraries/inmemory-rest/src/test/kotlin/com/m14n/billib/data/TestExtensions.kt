package com.m14n.billib.data

import com.m14n.billib.data.artist.DuplicateArtistDao
import java.util.regex.Matcher
import java.util.regex.Pattern

fun String.sameArtist(artist: String, duplicateArtistDao: DuplicateArtistDao? = null, strict: Boolean = true): Boolean {
    val c1 = toLowerCase().asCollaboration.map { it.meaningful }.toSet()
    val c2 = (duplicateArtistDao?.findByName(artist)?.name ?: artist)
        .toLowerCase().asCollaboration.map { single ->
            duplicateArtistDao?.findByName(single)?.name?.toLowerCase() ?: single.toLowerCase()
        }.map { it.meaningful }.toSet()
    return if (strict) c1 == c2 else c1.containsAll(c2)
}

val String.asCollaboration: List<String>
    get() {
        return split(
            ("( & )" + "|(, )" + "|( and )" + "|( or )" + "|( with )" + "|( vs\\.? )" + "|( feat\\.? )" + "|( ft\\.? )" +
                    "|( y )" + "|( / )" + "|( presents )" + "|( pres\\.? )" + "|( starr?ing )" +
                    "|( introducing )" + "| ( \\+ )").toRegex()
        )
    }

val String.meaningful: String
    get() = this.replace("[^A-Za-z0-9]+".toRegex(), "")

val String.optimized: String
    get() = optimizeFeaturing.optimizeFeat.optimizeDuet.optimizeWith

// Artist might be "The Miracles (featuring Bill Smokey Robinson)"
private val PATTERN_FEATURING = Pattern.compile("\\([F|f]eaturing (.+)\\)")
private val String.optimizeFeaturing: String
    get() {
        val m: Matcher = PATTERN_FEATURING.matcher(this)
        if (m.find()) {
            val duet = m.group(1)
            val toReplace = m.group()
            return replace(toReplace, "feat. $duet")
        }
        return replace("(?i) featuring ".toRegex(), " feat. ")
    }

// Artist might be "The Miracles (featuring Bill Smokey Robinson)"
private val PATTERN_FEAT = Pattern.compile("\\([F|f]eat\\. (.+)\\)")
private val String.optimizeFeat: String
    get() {
        val m: Matcher = PATTERN_FEAT.matcher(this)
        if (m.find()) {
            val duet = m.group(1)
            val toReplace = m.group()
            return replace(toReplace, "feat. $duet")
        }
        return this
    }


// Artist might be "Chubby Checker (with Dee Dee Sharp)"
private val PATTERN_WITH = Pattern.compile("\\([W|w]ith (.+)\\)")
private val String.optimizeWith: String
    get() {
        val m: Matcher = PATTERN_WITH.matcher(this)
        if (m.find()) {
            val duet = m.group(1)
            val toReplace = m.group()
            return replace(toReplace, "& $duet")
        }
        return replace("(?i) with ".toRegex(), " & ")
    }

private val PATTERN_DUET_WITH = Pattern.compile("\\([D|d]uet [W|w]ith (.+)\\)")
private val String.optimizeDuet: String
    get() {
        val m: Matcher = PATTERN_DUET_WITH.matcher(this)
        if (m.find()) {
            val duet = m.group(1)
            val toReplace = m.group()
            return replace(toReplace, "& $duet")
        }
        return replace("(?i) duet with ".toRegex(), " & ")
    }