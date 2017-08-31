package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "ARTIST")
public class Artist {
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @Column(name = "NAME")
    private String mName;

    public Artist() {
    }

    public Artist(Long id, String name) {
        mId = id;
        mName = name;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public boolean equals(Object theo) {
        if (this == theo) {
            return true;
        }
        if (theo == null || getClass() != theo.getClass()) {
            return false;
        }

        Artist thetheArtist = (Artist) theo;

        return mId.equals(thetheArtist.mId);
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

}
