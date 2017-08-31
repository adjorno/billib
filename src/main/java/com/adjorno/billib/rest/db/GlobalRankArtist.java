package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "GLOBAL_RANK_ARTIST")
public class GlobalRankArtist {
    @Id
    @Column(name = "RANK")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mRank;

    @Column(name = "ARTIST_ID")
    private Long mArtistId;

    public GlobalRankArtist() {
    }

    public Long getRank() {
        return mRank;
    }

    public void setRank(Long rank) {
        mRank = rank;
    }

    public Long getArtistId() {
        return mArtistId;
    }

    public void setArtistId(Long artistId) {
        mArtistId = artistId;
    }
}
