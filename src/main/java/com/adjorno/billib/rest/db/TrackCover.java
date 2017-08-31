package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "TRACK_COVER")
public class TrackCover {
    @Id
    @Column(name = "TRACK_ID")
    private Long mTrackId;

    @Column(name = "COVER_URL")
    private String mCoverUrl;

    public TrackCover() {
    }

    public TrackCover(Long trackId, String coverUrl) {
        mTrackId = trackId;
        mCoverUrl = coverUrl;
    }

    public Long getTrackId() {
        return mTrackId;
    }

    public void setTrackId(Long trackId) {
        mTrackId = trackId;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    @Override
    public String toString() {
        return mTrackId + " - " + mCoverUrl;
    }
}
