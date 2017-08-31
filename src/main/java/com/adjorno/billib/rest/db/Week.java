package com.adjorno.billib.rest.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "WEEK")
public class Week {
    @Id
    @Column(name = "WEEK_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @Column(name = "DATE")
    private String mDate;

    public Week() {
    }

    public Week(Long id, String date) {
        mId = id;
        mDate = date;
    }

    @JsonIgnore
    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    @Override
    public String toString() {
        return mDate;
    }
}
