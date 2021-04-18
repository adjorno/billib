package com.m14n.billib.data.artist

import com.m14n.billib.data.dao.Dao
import com.m14n.billib.data.dao.FindByName
import com.m14n.billib.data.dao.collections.FindByNameMapStrategy
import com.m14n.billib.data.dao.collections.collectionsDao

fun artistDao(artists: Collection<Artist>) = ArtistDaoDelegate(
    collectionsDao(artists),
    FindByNameMapStrategy(artists)
)

interface ArtistDao :
    Dao<Artist>,
    FindByName<Artist>

class ArtistDaoDelegate(
    dao: Dao<Artist>,
    findByName: FindByName<Artist>
) : ArtistDao,
    Dao<Artist> by dao,
    FindByName<Artist> by findByName
