package com.ifochka.billib.rest.db

import java.util.regex.Pattern

object ArtistUtils {
    val PATTERN_DUET_WITH: Pattern = Pattern.compile("\\([D|d]uet [W|w]ith (.+)\\)")
    val PATTERN_WITH: Pattern = Pattern.compile("\\([W|w]ith (.+)\\)")
    val PATTERN_FEATURING: Pattern = Pattern.compile("\\([F|f]eaturing (.+)\\)")

    @JvmStatic
    fun equalsEasy(
        a1: String,
        a2: String,
        artistRepository: ArtistRepository,
        duplicateArtistRepository: DuplicateArtistRepository,
    ): Boolean = a1.equals(a2, ignoreCase = true)

    @JvmStatic
    fun equals(
        a1: String,
        a2: String,
        artistRepository: ArtistRepository,
        duplicateArtistRepository: DuplicateArtistRepository,
    ): Boolean {
        val aa1 = artistAlternatives(a1, artistRepository, duplicateArtistRepository)
        val aa2 = artistAlternatives(a2, artistRepository, duplicateArtistRepository)
        for (i in aa1.indices) {
            for (j in aa2.indices) {
                if (aa1[i].equals(aa2[j], ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun artistAlternatives(
        artistName: String,
        artistRepository: ArtistRepository,
        duplicateArtistRepository: DuplicateArtistRepository,
    ): Array<String> {
        val theResult = mutableListOf<String>()
        val theArtist = artistRepository.findByName(artistName)
        theResult.add(artistName)
        if (theArtist != null) {
            val theDuplicates = duplicateArtistRepository.findByArtist(theArtist)
            for (theDuplicateArtist in theDuplicates) {
                theDuplicateArtist.duplicateName?.let { theResult.add(it) }
            }
        }
        return theResult.toTypedArray()
    }

    @JvmStatic
    fun optimizeName(original: String): String =
        optimizeFeaturing(
            optimizeWith(
                optimizeDuetWith(
                    original.replace("(?i) featuring ".toRegex(), " feat. ")
                        .replace("(?i) with ".toRegex(), " & ")
                        .replace("(?i) / ".toRegex(), " & "),
                ),
            ),
        )

    @JvmStatic
    fun optimizeDuetWith(artistName: String): String {
        val m = PATTERN_DUET_WITH.matcher(artistName)
        if (m.find()) {
            val duet = m.group(1) ?: return artistName
            val toReplace = m.group()
            return artistName.replace("\\($toReplace\\)".toRegex(), "& $duet")
        }
        return artistName
    }

    @JvmStatic
    fun optimizeWith(artistName: String): String {
        val m = PATTERN_WITH.matcher(artistName)
        if (m.find()) {
            val duet = m.group(1)
            val toReplace = m.group()
            return artistName.replace("\\($toReplace\\)".toRegex(), "& $duet")
        }
        return artistName
    }

    @JvmStatic
    fun optimizeFeaturing(artistName: String): String {
        val m = PATTERN_FEATURING.matcher(artistName)
        if (m.find()) {
            val duet = m.group(1)
            val toReplace = m.group()
            return artistName.replace("\\($toReplace\\)".toRegex(), "feat. $duet")
        }
        return artistName
    }

    @JvmStatic
    fun splitCollaboration(artist: String): Array<String> =
        artist.lowercase()
            .split(
                "( & )|" +
                    "(, )|" +
                    "( and )|" +
                    "( vs\\.? )|" +
                    "( feat\\.? )|" +
                    "( y )|" +
                    "( / )|" +
                    "( presents )|" +
                    "( pres\\.? )|" +
                    "( starr?ing )|" +
                    "( introducing )|" +
                    "| ( \\+ )".toRegex(),
            ).toTypedArray()

    /**
     * Returns all collaboration artists (e.g., "A, B, C") from the relations.
     */
    @JvmStatic
    fun asCollaborationArtists(artistRelations: List<ArtistRelation>): List<Artist> =
        artistRelations.mapNotNull { it.collaborationArtist }

    /**
     * Returns all member artists (individual artists who are part of collaborations).
     */
    @JvmStatic
    fun asMemberArtists(artistRelations: List<ArtistRelation>): List<Artist> =
        artistRelations.mapNotNull { it.memberArtist }

    /**
     * For a given artist, extracts all collaborating artists from the relations.
     * - If artist is a collaboration (e.g., "A, B, C"), returns all its members: [A, B, C]
     * - If artist is a member, returns all OTHER members from the same collaborations
     *
     * Note: For the second case, this only works if artistRelations includes ALL relations
     * for the collaborations, not just relations involving excludeArtist.
     */
    @JvmStatic
    fun extractCollaborators(
        artistRelations: List<ArtistRelation>,
        excludeArtist: Artist,
    ): List<Artist> {
        val collaborators = mutableSetOf<Artist>()

        // Case 1: Artist is a collaboration - return all its members
        artistRelations.filter { it.collaborationArtist?.id == excludeArtist.id }
            .forEach { it.memberArtist?.let { member -> collaborators.add(member) } }

        // Case 2: Artist is a member - find all other members from same collaborations
        val collaborationsAsMember = artistRelations
            .filter { it.memberArtist?.id == excludeArtist.id }
            .mapNotNull { it.collaborationArtist }
            .toSet()

        // For each collaboration this artist is a member of, add all other members
        collaborationsAsMember.forEach { collab ->
            artistRelations.filter {
                it.collaborationArtist?.id == collab.id && it.memberArtist?.id != excludeArtist.id
            }
                .forEach { it.memberArtist?.let { member -> collaborators.add(member) } }
        }

        return collaborators.toList()
    }

    @JvmStatic
    fun asArtistIds(artists: List<Artist>): List<Long> = artists.mapNotNull { it.id }

    @JvmStatic
    fun getSearchQuery(
        keywords: Array<String>,
        offset: Int,
        limit: Int,
        alphabetical: Boolean,
    ): String {
        val theQueryBuilder = StringBuilder("SELECT ARTIST._ID, ARTIST.NAME, ARTIST.NAME_NORMALIZED FROM ARTIST")
        if (!alphabetical) {
            theQueryBuilder.append(" JOIN GLOBAL_RANK_ARTIST ON ARTIST._ID = GLOBAL_RANK_ARTIST.ARTIST_ID")
        }
        var where = false
        for (keyWord in keywords) {
            if (!where) {
                theQueryBuilder.append(" WHERE")
                where = true
            } else {
                theQueryBuilder.append(" AND")
            }
            theQueryBuilder.append(" ARTIST.NAME LIKE '%").append(keyWord.replace("'", "''")).append("%'")
        }
        theQueryBuilder.append(" ORDER BY ").append(
            if (alphabetical) {
                "ARTIST.NAME ASC"
            } else {
                "GLOBAL_RANK_ARTIST.RANK ASC"
            },
        )
        if (limit != 0) {
            theQueryBuilder.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset)
        }
        return theQueryBuilder.toString()
    }

    @JvmStatic
    fun getCountSearchQuery(keywords: Array<String>): String {
        val theQueryBuilder = StringBuilder("SELECT COUNT(*) FROM ARTIST")
        var where = false
        for (keyWord in keywords) {
            if (!where) {
                theQueryBuilder.append(" WHERE")
                where = true
            } else {
                theQueryBuilder.append(" AND")
            }
            theQueryBuilder.append(" ARTIST.NAME LIKE '%").append(keyWord.replace("'", "''")).append("%'")
        }
        return theQueryBuilder.toString()
    }

    @JvmStatic
    fun asArtists(tracks: List<Track>): List<Artist> = tracks.mapNotNull { it.artist }
}
