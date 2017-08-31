package com.adjorno.billib.rest.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "CHART_LIST")
public class ChartList {
    @Id
    @Column(name = "_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mId;

    @OneToOne
    @JoinColumn(name = "CHART_ID")
    private Chart mChart;

    @OneToOne
    @JoinColumn(name = "WEEK_ID")
    private Week mWeek;

    @Column(name = "NUMBER")
    private Integer mNumber;

    @Column(name = "PREVIOUS_CHART_LIST_ID")
    private Long mPreviousChartListId;

    @Transient
    private List<ChartTrack> mChartTracks;

    public ChartList() {
    }

    public ChartList(Long id, Chart chart, Week week, Integer number, Long previousChartListId) {
        mId = id;
        mChart = chart;
        mWeek = week;
        mNumber = number;
        mPreviousChartListId = previousChartListId;
    }

    public List<ChartTrack> getChartTracks() {
        return mChartTracks;
    }

    public void setChartTracks(List<ChartTrack> chartTracks) {
        mChartTracks = chartTracks;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public Chart getChart() {
        return mChart;
    }

    public void setChart(Chart chart) {
        mChart = chart;
    }

    public Week getWeek() {
        return mWeek;
    }

    public void setWeek(Week week) {
        mWeek = week;
    }

    @JsonIgnore
    public Integer getNumber() {
        return mNumber;
    }

    public void setNumber(Integer number) {
        mNumber = number;
    }

    @JsonIgnore
    public Long getPreviousChartListId() {
        return mPreviousChartListId;
    }

    public void setPreviousChartListId(Long previousChartListId) {
        mPreviousChartListId = previousChartListId;
    }

    @Override
    public String toString() {
        return mNumber + ". " + mChart.toString() + " " + mWeek.toString();
    }
}
