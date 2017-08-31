package com.adjorno.billib.rest.model;

import com.google.gson.annotations.SerializedName;

public class Trends {
    @SerializedName("week")
    private String mWeek;

    private TrendList[] mTrendLists;

    public Trends(String week) {
        mWeek = week;
    }

    public String getWeek() {
        return mWeek;
    }

    public void setWeek(String week) {
        mWeek = week;
    }

    public TrendList[] getTrendLists() {
        return mTrendLists;
    }

    public void setTrendLists(TrendList[] trendLists) {
        mTrendLists = trendLists;
    }
}
