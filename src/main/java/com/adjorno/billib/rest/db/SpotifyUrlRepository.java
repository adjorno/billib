package com.adjorno.billib.rest.db;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpotifyUrlRepository extends CrudRepository<SpotifyUrl, Long> {
    List<SpotifyUrl> findBymSpotifyUrl(String spotifyUrl);

    @Query(value = "select su from SpotifyUrl su where su.mTrackId in ?1")
    List<SpotifyUrl> findCoversByTrackIds(List<Long> trackIds);

    @Modifying
    @Query(value = "update SpotifyUrl su set su.mTrackId = ?2 where su.mTrackId = ?1")
    void updateTrackIds(Long from, Long to);
}
