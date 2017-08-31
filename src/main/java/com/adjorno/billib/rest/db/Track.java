package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "TRACK")
public class Track {
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @Column(name = "TITLE")
    private String mTitle;

    @OneToOne
    @JoinColumn(name = "ARTIST_ID")
    private Artist mArtist;

    @Transient
    private String mCoverUrl;

    @Transient
    private String mSpotifyUrl;

    public Track(Long mId, String mTitle, Artist mArtist) {
        this.mId = mId;
        this.mTitle = mTitle;
        this.mArtist = mArtist;
    }

    public Track() {
    }

    public String getSpotifyUrl() {
        return mSpotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        mSpotifyUrl = spotifyUrl;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Artist getArtist() {
        return mArtist;
    }

    public void setArtist(Artist artist) {
        mArtist = artist;
    }

    @Override
    public String toString() {
        return mArtist.getName() + " - " + mTitle;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    @Override
    public boolean equals(Object theo) {
        if (this == theo) {
            return true;
        }
        if (theo == null || getClass() != theo.getClass()) {
            return false;
        }

        Track thetheTrack = (Track) theo;

        return mId.equals(thetheTrack.mId);
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }
}
