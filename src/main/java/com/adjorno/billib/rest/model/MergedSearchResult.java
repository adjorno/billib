package com.adjorno.billib.rest.model;

import com.adjorno.billib.rest.db.Artist;
import com.adjorno.billib.rest.db.Track;

public class MergedSearchResult {
    SearchResult<Artist> mArtists;

    SearchResult<Track> mTracks;

    public SearchResult<Artist> getArtists() {
        return mArtists;
    }

    public void setArtists(SearchResult<Artist> artists) {
        mArtists = artists;
    }

    public SearchResult<Track> getTracks() {
        return mTracks;
    }

    public void setTracks(SearchResult<Track> tracks) {
        mTracks = tracks;
    }
}
