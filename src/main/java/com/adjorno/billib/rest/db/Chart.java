package com.adjorno.billib.rest.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "CHART")
public class Chart {
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @Column(name = "NAME")
    private String mName;

    @OneToOne
    @JoinColumn(name = "JOURNAL_ID")
    private Journal mJournal;

    @Column(name = "LIST_SIZE")
    private Integer mListSize;

    @Column(name = "START_DATE")
    private String mStartDate;

    public Chart() {
    }

    public Chart(Long id, String name, Journal journal, Integer listSize, String startDate) {
        mId = id;
        mName = name;
        mJournal = journal;
        mListSize = listSize;
        mStartDate = startDate;
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

    public Journal getJournal() {
        return mJournal;
    }

    public void setJournal(Journal journal) {
        mJournal = journal;
    }

    public Integer getListSize() {
        return mListSize;
    }

    public void setListSize(Integer listSize) {
        mListSize = listSize;
    }

    public String getStartDate() {
        return mStartDate;
    }

    public void setStartDate(String startDate) {
        mStartDate = startDate;
    }

    @Override
    public String toString() {
        return mName;
    }
}
