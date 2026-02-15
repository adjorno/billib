package com.adjorno.billib.rest

import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import com.adjorno.billib.rest.db.*
import com.adjorno.billib.rest.model.ArtistInfo
import com.m14n.ex.Ex
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.util.ArrayList

@RestController
open class ArtistController {

    @Autowired
    private lateinit var artistRepository: ArtistRepository

    @Autowired
    private lateinit var duplicateArtistRepository: DuplicateArtistRepository

    @Autowired
    private lateinit var artistRelationRepository: ArtistRelationRepository

    @Autowired
    private lateinit var globalRankArtistRepository: GlobalRankArtistRepository

    @Autowired
    private lateinit var trackController: TrackController

    @RequestMapping(value = ["/artist/getById"], method = [RequestMethod.GET])
    fun getById(@RequestParam id: Long): Artist {
        return artistRepository.findByIdOrNull(id) ?: throw ArtistNotFoundException()
    }

    @RequestMapping(value = ["/artist/info"], method = [RequestMethod.GET])
    fun getInfo(
        @RequestParam id: Long,
        @RequestParam(name = "relations_size", required = false, defaultValue = "5") relationsSize: Int,
        @RequestParam(name = "tracks_size", required = false, defaultValue = "5") tracksSize: Int
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
        @RequestParam(required = false, defaultValue = "1") size: Long
    ): List<Artist> {
        return artistRepository.findGlobalList(rank, rank + size)
    }

    @RequestMapping(value = ["/artist/relations"], method = [RequestMethod.GET])
    fun getRelations(
        @RequestParam id: Long,
        @RequestParam(required = false, defaultValue = "0") size: Int
    ): List<Artist> {
        val theArtist = artistRepository.findByIdOrNull(id) ?: throw ArtistNotFoundException()

        // Find all collaborating artists (regardless of artist1/artist2 position)
        val allRelations = artistRelationRepository.findByArtist(theArtist)
        val collaborators = ArtistUtils.extractCollaborators(allRelations, theArtist)

        if (collaborators.isEmpty()) {
            return emptyList()
        }

        // Sort by global rank and limit if requested
        val requestedSize = if (size == 0) collaborators.size else size
        return artistRepository.sortByGlobalRank(
            ArtistUtils.asArtistIds(collaborators),
            requestedSize
        )
    }

    @Transactional
    @RequestMapping(value = ["/artist/rename"], method = [RequestMethod.POST])
    open fun rename(@RequestParam id: Long, @RequestParam(name = "name") newName: String) {
        val theArtist = artistRepository.findByIdOrNull(id) ?: throw ArtistNotFoundException()
        val theDuplicate = duplicateArtistRepository.findByDuplicateName(newName)
        val theOldName = theArtist.name
        if (theDuplicate != null) {
            if (theDuplicate.artist?.id != id) {
                println("WARNING! Did not rename ($theOldName) => ($newName)")
                return
            }
            duplicateArtistRepository.delete(theDuplicate)
            println("DUPLICATE REMOVED!")
        }
        artistRepository.rename(theArtist, newName)
        duplicateArtistRepository.save(DuplicateArtist(theOldName, theArtist))
        println("RENAMED! $theOldName => $newName")
    }

    open fun findArtist(artistName: String): Artist? {
        var theArtist = artistRepository.findByName(artistName)
        if (theArtist == null) {
            val theDuplicate = duplicateArtistRepository.findByDuplicateName(artistName)
            if (theDuplicate != null) {
                println("FOUND DUPLICATE Artist: " + artistName + " => " + theDuplicate.artist?.name)
                theArtist = theDuplicate.artist
            }
        }
        return theArtist
    }
}