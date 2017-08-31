package com.adjorno.billib.rest.db;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "CHART_TRACK")
public class ChartTrack {
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @OneToOne
    @JoinColumn(name = "TRACK_ID")
    private Track mTrack;

    @OneToOne
    @JoinColumn(name = "CHART_LIST_ID")
    private ChartList mChartList;

    @Column(name = "RANK")
    private int mRank;

    @Column(name = "LAST_WEEK_RANK")
    private int mLastWeekRank;

    public ChartTrack() {
    }

    public ChartTrack(Long id, Track track, ChartList chartList, int rank, int lastWeekRank) {
        mId = id;
        mTrack = track;
        mChartList = chartList;
        mRank = rank;
        mLastWeekRank = lastWeekRank;
    }

    @JsonIgnore
    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public Track getTrack() {
        return mTrack;
    }

    public void setTrack(Track track) {
        mTrack = track;
    }

    @JsonIgnore
    public ChartList getChartList() {
        return mChartList;
    }

    public void setChartList(ChartList chartList) {
        mChartList = chartList;
    }

    public int getRank() {
        return mRank;
    }

    public void setRank(int rank) {
        mRank = rank;
    }

    public int getLastWeekRank() {
        return mLastWeekRank;
    }

    public void setLastWeekRank(int lastWeekRank) {
        mLastWeekRank = lastWeekRank;
    }

    @Override
    public String toString() {
        return mChartList.toString() + " - " + mRank + ". " + mTrack.toString();
    }
}
