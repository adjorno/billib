package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "TREND_TRACK")
public class TrendTrack {
    @Id
    @Column(name = "_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @OneToOne
    @JoinColumn(name = "WEEK_ID")
    private Week mWeek;

    @OneToOne
    @JoinColumn(name = "TRACK_ID")
    private Track mTrack;

    @OneToOne
    @JoinColumn(name = "TYPE_ID")
    private TrendType mType;

    public TrendTrack() {
    }

    public TrendTrack(Long id, Week week, Track track, TrendType type) {
        mId = id;
        mWeek = week;
        mTrack = track;
        mType = type;

    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public Week getWeek() {
        return mWeek;
    }

    public void setWeek(Week week) {
        mWeek = week;
    }

    public Track getTrack() {
        return mTrack;
    }

    public void setTrack(Track track) {
        mTrack = track;
    }

    public TrendType getType() {
        return mType;
    }

    public void setType(TrendType type) {
        mType = type;
    }
}
