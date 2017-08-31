package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "DUPLICATE_ARTIST")
public class DuplicateArtist {
    @Id
    @Column(name = "DUPLICATE_NAME")
    private String mDuplicateName;

    @OneToOne
    @JoinColumn(name = "ARTIST_ID")
    private Artist mArtist;

    public DuplicateArtist() {
    }

    public DuplicateArtist(String duplicateName, Artist artist) {
        mDuplicateName = duplicateName;
        mArtist = artist;
    }

    public String getDuplicateName() {
        return mDuplicateName;
    }

    public void setDuplicateName(String duplicateName) {
        mDuplicateName = duplicateName;
    }

    public Artist getArtist() {
        return mArtist;
    }

    public void setArtist(Artist artist) {
        mArtist = artist;
    }
}
