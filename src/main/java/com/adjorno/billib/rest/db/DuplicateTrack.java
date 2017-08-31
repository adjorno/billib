package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "DUPLICATE_TRACK")
public class DuplicateTrack {
    @Id
    @Column(name = "DUPLICATE_TITLE")
    private String mDuplicateTitle;

    @OneToOne
    @JoinColumn(name = "TRACK_ID")
    private Track mTrack;

    public DuplicateTrack() {

    }

    public String getDuplicateTitle() {
        return mDuplicateTitle;
    }

    public void setDuplicateTitle(String duplicateTitle) {
        mDuplicateTitle = duplicateTitle;
    }

    public void setTrack(Track track) {
        mTrack = track;
    }

    public DuplicateTrack(String duplicateTitle, Track track) {
        mDuplicateTitle = duplicateTitle;
        mTrack = track;
    }

    public Track getTrack() {
        return mTrack;
    }

    public static String generateDuplicateTitle(Artist artist, String title) {
        return artist.getName() + " - " + title;
    }

    public static String[] splitArtistAndTitle(String duplicateTitle) {
        return duplicateTitle.split(" - ");
    }
}
