package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "JOURNAL")
public class Journal {
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @Column(name = "NAME")
    private String mName;

    public Journal() {
    }

    public Journal(Long id, String name) {
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
}
