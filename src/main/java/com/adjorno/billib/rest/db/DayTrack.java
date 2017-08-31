package com.adjorno.billib.rest.db;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "DAY_TRACK")
public class DayTrack {
    @Id
    @Column(name = "DAY")
    private Date mDay;

    @OneToOne
    @JoinColumn(name = "TRACK_ID")
    private Track mTrack;

    @Column(name = "DESCRIPTION")
    private String mDesc;

    public DayTrack() {
    }

    public DayTrack(Date day, Track track, String desc) {
        mDay = day;
        mTrack = track;
        mDesc = desc;
    }

    public Date getDay() {
        return mDay;
    }

    public void setDay(Date day) {
        mDay = day;
    }

    public Track getTrack() {
        return mTrack;
    }

    public void setTrack(Track track) {
        mTrack = track;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String desc) {
        mDesc = desc;
    }
}
