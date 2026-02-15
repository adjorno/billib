package com.adjorno.billib.rest.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface DuplicateArtistRepository : CrudRepository<DuplicateArtist, String> {
    @Modifying
    @Query(value = "update DuplicateArtist da set da.artist = ?2 where da.artist = ?1")
    fun updateArtists(from: Artist, to: Artist)

    fun findByDuplicateName(name: String): DuplicateArtist?

    fun findByArtist(artist: Artist): List<DuplicateArtist>

    @Modifying
    @Query(value = "update DuplicateArtist da set da.duplicateName = ?2 where da = ?1")
    fun rename(duplicateArtist: DuplicateArtist, optimizedName: String)
}
