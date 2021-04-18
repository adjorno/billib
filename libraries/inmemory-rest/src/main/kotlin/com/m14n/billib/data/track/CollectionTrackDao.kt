package com.m14n.billib.data.track

import com.m14n.billib.data.artist.Artist
import com.m14n.billib.data.dao.collections.collectionsDao

fun collectionTrackDao(tracks: Collection<Track>) = TrackDaoDelegate(
    collectionsDao(tracks),
    CollectionTrackDao(tracks)
)

class CollectionTrackDao(
    private val tracks: Collection<Track>
) : FindByArtistAndTitle {

    override fun findByArtistAndTitle(artist: Artist, title: String) =
        tracks.firstOrNull { trackEntry ->
            (trackEntry.artist == artist) && (trackEntry.title == title)
        }
}
