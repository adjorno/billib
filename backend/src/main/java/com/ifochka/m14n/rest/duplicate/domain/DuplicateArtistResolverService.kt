package com.ifochka.m14n.rest.duplicate.domain

import com.ifochka.m14n.rest.catalog.domain.Artist
import com.ifochka.m14n.rest.catalog.domain.ArtistDuplicateResolver
import org.springframework.stereotype.Service

@Service
class DuplicateArtistResolverService(
    private val duplicateArtistRepository: DuplicateArtistRepository,
) : ArtistDuplicateResolver {
    override fun findByAlternativeName(name: String): Artist? =
        duplicateArtistRepository.findByDuplicateName(name)?.artist

    override fun prepareRename(
        artist: Artist,
        newName: String,
    ): Boolean {
        val existing = duplicateArtistRepository.findByDuplicateName(newName) ?: return true
        if (existing.artist?.id != artist.id) {
            println("WARNING! Did not rename (${artist.name}) => ($newName)")
            return false
        }
        duplicateArtistRepository.delete(existing)
        println("DUPLICATE REMOVED!")
        return true
    }

    override fun recordRename(
        artist: Artist,
        oldName: String,
    ) {
        duplicateArtistRepository.save(DuplicateArtist(oldName, artist))
    }
}
