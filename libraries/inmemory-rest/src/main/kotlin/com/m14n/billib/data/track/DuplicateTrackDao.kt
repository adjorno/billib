package com.m14n.billib.data.track

import com.m14n.billib.data.dao.FindByName
import com.m14n.billib.data.dao.collections.FindByNameMapStrategy

fun duplicateTrackDao(fullTitleToTrack: Map<String, Track>) = DuplicateTrackDaoDelegate(
    FindByNameMapStrategy(fullTitleToTrack)
)

interface DuplicateTrackDao :
    FindByName<Track>

class DuplicateTrackDaoDelegate(
    findByName: FindByName<Track>
) : DuplicateTrackDao,
    FindByName<Track> by findByName
