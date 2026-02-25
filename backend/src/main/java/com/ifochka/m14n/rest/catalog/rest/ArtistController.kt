package com.ifochka.m14n.rest.catalog.rest

import com.ifochka.m14n.rest.catalog.domain.Artist
import com.ifochka.m14n.rest.catalog.domain.ArtistDuplicateResolver
import com.ifochka.m14n.rest.catalog.domain.ArtistRelationRepository
import com.ifochka.m14n.rest.catalog.domain.ArtistRepository
import com.ifochka.m14n.rest.catalog.domain.ArtistUtils
import com.ifochka.m14n.rest.catalog.rest.dtos.ArtistInfo
import com.ifochka.m14n.rest.rankings.domain.GlobalRankArtistRepository
import com.ifochka.m14n.rest.shared.ArtistNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
open class ArtistController(
    private val artistRepository: ArtistRepository,
    private val artistDuplicateResolver: ArtistDuplicateResolver,
    private val artistRelationRepository: ArtistRelationRepository,
    private val globalRankArtistRepository: GlobalRankArtistRepository,
    private val trackController: TrackController,
) {
    @RequestMapping(value = ["/artist/getById"], method = [RequestMethod.GET])
    fun getById(
        @RequestParam id: Long,
    ): Artist = artistRepository.findByIdOrNull(id) ?: throw ArtistNotFoundException()

    @RequestMapping(value = ["/artist/info"], method = [RequestMethod.GET])
    fun getInfo(
        @RequestParam id: Long,
        @RequestParam(name = "relations_size", required = false, defaultValue = "5") relationsSize: Int,
        @RequestParam(name = "tracks_size", required = false, defaultValue = "5") tracksSize: Int,
    ): ArtistInfo {
        val theArtist = artistRepository.findByIdOrNull(id) ?: throw ArtistNotFoundException()
        val theInfo = ArtistInfo()
        theInfo.artist = theArtist
        theInfo.globalRank = theArtist.id?.let { globalRankArtistRepository.findByArtistId(it)?.rank } ?: 0
        theInfo.artistRelations = getRelations(id, relationsSize)
        theInfo.tracks = trackController.getTracks(theArtist, tracksSize)
        return theInfo
    }

    @RequestMapping(value = ["/artist/global"], method = [RequestMethod.GET])
    fun getGlobalArtists(
        @RequestParam rank: Long,
        @RequestParam(required = false, defaultValue = "1") size: Long,
    ): List<Artist> = artistRepository.findGlobalList(rank, rank + size)

    @RequestMapping(value = ["/artist/relations"], method = [RequestMethod.GET])
    fun getRelations(
        @RequestParam id: Long,
        @RequestParam(required = false, defaultValue = "0") size: Int,
    ): List<Artist> {
        val theArtist = artistRepository.findByIdOrNull(id) ?: throw ArtistNotFoundException()

        val directRelations = artistRelationRepository.findByArtist(theArtist)

        val collaborationArtists = directRelations
            .filter { it.memberArtist?.id == theArtist.id }
            .mapNotNull { it.collaborationArtist }

        val allRelations = if (collaborationArtists.isNotEmpty()) {
            val allCollabRelations = collaborationArtists.flatMap {
                artistRelationRepository.findByCollaborationArtist(it)
            }
            (directRelations + allCollabRelations).distinct()
        } else {
            directRelations
        }

        val collaborators = ArtistUtils.extractCollaborators(allRelations, theArtist)

        if (collaborators.isEmpty()) {
            return emptyList()
        }

        val requestedSize = if (size == 0) collaborators.size else size
        return artistRepository.sortByGlobalRank(
            ArtistUtils.asArtistIds(collaborators),
            requestedSize,
        )
    }

    @Transactional
    @RequestMapping(value = ["/artist/rename"], method = [RequestMethod.POST])
    open fun rename(
        @RequestParam id: Long,
        @RequestParam(name = "name") newName: String,
    ) {
        val theArtist = artistRepository.findByIdOrNull(id) ?: throw ArtistNotFoundException()
        val theOldName = theArtist.name
        if (!artistDuplicateResolver.prepareRename(theArtist, newName)) return
        artistRepository.rename(theArtist, newName)
        artistDuplicateResolver.recordRename(theArtist, theOldName!!)
        println("RENAMED! $theOldName => $newName")
    }

    open fun findArtist(artistName: String): Artist? {
        var theArtist = artistRepository.findByName(artistName)
        if (theArtist == null) {
            theArtist = artistDuplicateResolver.findByAlternativeName(artistName)
            if (theArtist != null) {
                println("FOUND DUPLICATE Artist: $artistName => ${theArtist.name}")
            }
        }
        return theArtist
    }
}
