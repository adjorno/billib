package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "GLOBAL_RANK_TRACK")
public class GlobalRankTrack {
    @Id
    @Column(name = "RANK")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mRank;

    @Column(name = "TRACK_ID")
    private Long mTrackId;

    public Long getRank() {
        return mRank;
    }

    public void setRank(Long rank) {
        mRank = rank;
    }

    public Long getTrackId() {
        return mTrackId;
    }

    public void setTrackId(Long trackId) {
        mTrackId = trackId;
    }
}
