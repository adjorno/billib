package com.adjorno.billib.rest.db;

import javax.persistence.*;

@Entity
@Table(name = "TREND_TYPE")
public class TrendType {
    public static final long TYPE_ALL = 0;
    public static final long TYPE_GAINERS = 1;
    public static final long TYPE_DEBUTS = 2;
    public static final long TYPE_FUTURES = 3;
    public static final long TYPE_SENIORS = 4;

    @Id
    @Column(name = "_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @Column(name = "DESCRIPTION")
    private String mDescription;

    public TrendType() {
    }

    public TrendType(Long id, String description) {
        mId = id;
        mDescription = description;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }
}
