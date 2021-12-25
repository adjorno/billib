package com.adjorno.billib.rest.model;

import com.adjorno.billib.rest.db.Track;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TrendList {
    @SerializedName("name")
    private String mTrendName;

    @SerializedName("tracks")
    private List<Track> mTracks = new ArrayList<>();

    public TrendList(String trendName) {
        mTrendName = trendName;
    }

    public String getTrendName() {
        return mTrendName;
    }

    public void setTrendName(String trendName) {
        mTrendName = trendName;
    }

    public List<Track> getTracks() {
        return mTracks;
    }

    public void addTrack(Track track) {
        mTracks.add(track);
    }
}
