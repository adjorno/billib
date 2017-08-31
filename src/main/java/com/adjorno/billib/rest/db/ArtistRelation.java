package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "ARTIST_RELATION")
public class ArtistRelation {
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @OneToOne
    @JoinColumn(name = "SINGLE_ID")
    private Artist mSingle;

    @OneToOne
    @JoinColumn(name = "BAND_ID")
    private Artist mBand;

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public Artist getSingle() {
        return mSingle;
    }

    public void setSingle(Artist single) {
        mSingle = single;
    }

    public Artist getBand() {
        return mBand;
    }

    public void setBand(Artist band) {
        mBand = band;
    }

    public ArtistRelation(Artist single, Artist band) {
        mSingle = single;
        mBand = band;
    }

    public ArtistRelation() {
    }

}
