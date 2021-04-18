package com.m14n.billib.data.artist

import com.m14n.billib.data.dao.FindByName
import com.m14n.billib.data.dao.collections.FindByNameMapStrategy

fun duplicateArtistDao(nameToArtist: Map<String, Artist>) = DuplicateArtistDaoDelegate(
    FindByNameMapStrategy(nameToArtist)
)

interface DuplicateArtistDao :
    FindByName<Artist>

class DuplicateArtistDaoDelegate(
    findByName: FindByName<Artist>
) : DuplicateArtistDao,
    FindByName<Artist> by findByName
