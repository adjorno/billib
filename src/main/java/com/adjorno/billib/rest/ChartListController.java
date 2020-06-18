package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.*;
import com.m14n.ex.Ex;
import com.m14n.billib.data.BB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class ChartListController {

    @Autowired
    private ChartListRepository mChartListRepository;

    @Autowired
    private ChartTrackRepository mChartTrackRepository;

    @Autowired
    private WeekRepository mWeekRepository;

    @Autowired
    private ChartRepository mChartRepository;

    @Autowired
    private TrackRepository mTrackRepository;

    @Autowired
    private ChartTrackController mChartTrackController;

    @RequestMapping(value = "/chartList/getById", method = RequestMethod.GET)
    public ChartList getChartListById(@RequestParam(name = "id") Long chartListId) {
        final ChartList theChartList = mChartListRepository.findOne(chartListId);
        if (theChartList == null) {
            throw new ChartListNotFoundException();
        }
        return getPrintableChartList(theChartList);
    }

    @RequestMapping(value = "/chartList/getByDate", method = RequestMethod.GET)
    public ChartList getChartListByDate(@RequestParam(name = "chart_id") Long chartId,
            @RequestParam(required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) String date) {
        Chart theChart = mChartRepository.findOne(chartId);
        if (theChart == null) {
            throw new ChartListNotFoundException();
        }
        ChartList theChartList = null;
        if (Ex.isEmpty(date)) {
             theChartList = mChartListRepository.findLast(theChart, new PageRequest(0, 1)).getContent().get(0);
        } else {
            List<Week> theWeeks = mWeekRepository.findClosest(date);
            if (Ex.isNotEmpty(theWeeks)) {
                theChartList = mChartListRepository.findByChartAndWeek(theChart, theWeeks.get(0));
            }
        }
        if (theChartList == null) {
            throw new ChartListNotFoundException();
        }
        return getPrintableChartList(theChartList);
    }

    @RequestMapping(value = "/chartList/getFirstAppearance", method = RequestMethod.GET)
    public ChartList getFirstAppearance(@RequestParam(name = "track_id") Long trackId) {
        Track theTrack = mTrackRepository.findOne(trackId);
        if (theTrack == null) {
            throw new TrackNotFoundException();
        }
        ChartTrack theDebut = mChartTrackController.getDebut(theTrack);
        ChartList theChartList = theDebut.getChartList();
        theChartList.setChartTracks(Arrays.asList(theDebut));
        return theChartList;
    }

    private ChartList getPrintableChartList(ChartList chartList) {
        List<ChartTrack> theChartTracks = mChartTrackRepository.findByChartList(chartList);
        chartList.setChartTracks(theChartTracks);
        return chartList;
    }
}
