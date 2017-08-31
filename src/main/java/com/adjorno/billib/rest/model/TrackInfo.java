package com.adjorno.billib.rest.model;

import com.adjorno.billib.rest.db.Track;

import java.util.Map;

public class TrackInfo {
    private Track mTrack;

    private Map<String, Map<String, Integer>> mHistory;

    private long mGlobalRank;

    public TrackInfo() {
    }

    public Track getTrack() {
        return mTrack;
    }

    public void setTrack(Track track) {
        mTrack = track;
    }

    public Map<String, Map<String, Integer>> getHistory() {
        return mHistory;
    }

    public void setHistory(Map<String, Map<String, Integer>> history) {
        mHistory = history;
    }

    public long getGlobalRank() {
        return mGlobalRank;
    }

    public void setGlobalRank(long globalRank) {
        mGlobalRank = globalRank;
    }
}
