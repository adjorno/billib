package com.ifochka.billib.rest.db

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface ArtistRelationRepository : CrudRepository<ArtistRelation, Long> {

    @Modifying
    @Query(value = "update ArtistRelation ar set ar.collaborationArtist = ?2 where ar.collaborationArtist = ?1")
    fun updateCollaborationArtist(duplicateArtist: Artist, originalArtist: Artist)

    @Modifying
    @Query(value = "update ArtistRelation ar set ar.memberArtist = ?2 where ar.memberArtist = ?1")
    fun updateMemberArtist(duplicateArtist: Artist, originalArtist: Artist)

    @Query(value = "SELECT collaboration_artist_id FROM artist_relation WHERE member_artist_id IN (?1, ?2) GROUP BY collaboration_artist_id",
            nativeQuery = true)
    fun findMergingCollaborationIds(artistId1: Long, artistId2: Long): List<Long>

    @Query(value = "SELECT member_artist_id FROM artist_relation WHERE collaboration_artist_id IN (?1, ?2) GROUP BY member_artist_id",
            nativeQuery = true)
    fun findMergingMemberIds(artistId1: Long, artistId2: Long): List<Long>

    @Modifying
    @Query(value = "delete from ArtistRelation ar where ar.collaborationArtist.id = ?1 and ar.memberArtist.id = ?2")
    fun deleteByCollaborationIdAndMemberId(collaborationId: Long, memberId: Long)

    fun findByCollaborationArtist(artist: Artist): List<ArtistRelation>

    fun findByMemberArtist(artist: Artist): List<ArtistRelation>

    // Find all collaborations where the artist is a member
    @Query(value = "select ar from ArtistRelation ar where ar.memberArtist = ?1")
    fun findCollaborationsByMember(artist: Artist): List<ArtistRelation>

    // Find all members of collaborations that the artist is in (either as collaboration or as member)
    @Query(value = "select ar from ArtistRelation ar where ar.collaborationArtist = ?1 or ar.memberArtist = ?1")
    fun findByArtist(artist: Artist): List<ArtistRelation>
}
