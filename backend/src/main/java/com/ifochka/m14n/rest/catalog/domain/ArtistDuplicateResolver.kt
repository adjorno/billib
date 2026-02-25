package com.ifochka.m14n.rest.catalog.domain

interface ArtistDuplicateResolver {
    fun findByAlternativeName(name: String): Artist?

    /** Returns false if the rename is blocked by a different artist. */
    fun prepareRename(
        artist: Artist,
        newName: String,
    ): Boolean

    fun recordRename(
        artist: Artist,
        oldName: String,
    )
}
