package com.m14n.billib.data.track

import com.m14n.billib.data.artist.Artist
import com.m14n.billib.data.dao.Dao

/**
 * Generic dao for [Track]
 */
interface TrackDao :
    Dao<Track>,
    FindByArtistAndTitle

class TrackDaoDelegate(
    private val dao: Dao<Track>,
    private val findByArtistAndTitle: FindByArtistAndTitle
) : TrackDao,
    Dao<Track> by dao,
    FindByArtistAndTitle by findByArtistAndTitle

interface FindByArtistAndTitle {
    /**
     * @param artist Track artist to search for
     * @param title Track title to search for
     * @return [Track] with the given title performed by the given [Artist], null if not found
     */
    fun findByArtistAndTitle(artist: Artist, title: String): Track?
}
