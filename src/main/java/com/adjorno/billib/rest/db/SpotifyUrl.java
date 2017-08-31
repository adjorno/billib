package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "SPOTIFY_URL")
public class SpotifyUrl {
    @Id
    @Column(name = "TRACK_ID")
    private Long mTrackId;

    @Column(name = "SPOTIFY_URL")
    private String mSpotifyUrl;

    public Long getTrackId() {
        return mTrackId;
    }

    public void setTrackId(Long trackId) {
        mTrackId = trackId;
    }

    public String getSpotifyUrl() {
        return mSpotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        mSpotifyUrl = spotifyUrl;
    }

    @Override
    public String toString() {
        return mTrackId + " - " + mSpotifyUrl;
    }
}
