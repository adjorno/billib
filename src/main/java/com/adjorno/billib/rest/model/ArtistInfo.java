package com.adjorno.billib.rest.model;

import com.adjorno.billib.rest.db.Artist;
import com.adjorno.billib.rest.db.Track;

import java.util.List;

public class ArtistInfo {
    private Artist mArtist;

    private long mGlobalRank;

    private List<Artist> mArtistRelations;

    private List<Track> mTracks;

    public ArtistInfo() {
    }

    public ArtistInfo(Artist artist, long globalRank, List<Artist> artistRelations, List<Track> tracks) {
        mArtist = artist;
        mGlobalRank = globalRank;
        mArtistRelations = artistRelations;
        mTracks = tracks;
    }

    public Artist getArtist() {
        return mArtist;
    }

    public void setArtist(Artist artist) {
        mArtist = artist;
    }

    public long getGlobalRank() {
        return mGlobalRank;
    }

    public void setGlobalRank(long globalRank) {
        mGlobalRank = globalRank;
    }

    public List<Artist> getArtistRelations() {
        return mArtistRelations;
    }

    public void setArtistRelations(List<Artist> artistRelations) {
        mArtistRelations = artistRelations;
    }

    public List<Track> getTracks() {
        return mTracks;
    }

    public void setTracks(List<Track> tracks) {
        mTracks = tracks;
    }
}
