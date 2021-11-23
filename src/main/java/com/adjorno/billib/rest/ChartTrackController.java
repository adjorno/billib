package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.*;
import com.m14n.ex.Ex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class ChartTrackController {
    private static final String PASSWORD = "vtldtlm";

    @Autowired
    private ChartListRepository mChartListRepository;

    @Autowired
    ChartTrackRepository mChartTrackRepository;

    @Autowired
    TrackRepository mTrackRepository;

    @Transactional
    @RequestMapping(value = "/chartTrack/updateTrack", method = RequestMethod.POST)
    public void updateTrack(@RequestParam(name = "password") String password,
            @RequestParam(name = "chartTrackId") Long chartTrackId, @RequestParam(name = "trackId") Long trackId) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        Track theNewTrack = mTrackRepository.findById(trackId).orElse(null);
        ChartTrack theChartTrack = mChartTrackRepository.findById(chartTrackId).orElse(null);
        if (theChartTrack == null && theNewTrack == null) {
            throw new TrackNotFoundException();
        }
        mChartTrackRepository.updateTrack(theChartTrack, theNewTrack);
    }

    @Transactional
    @RequestMapping(value = "/chartTrack/updateRank", method = RequestMethod.POST)
    public void updateRank(@RequestParam(name = "password") String password,
            @RequestParam(name = "chartTrackId") Long chartTrackId, @RequestParam(name = "rank") int rank) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        ChartTrack theChartTrack = mChartTrackRepository.findById(chartTrackId).orElse(null);
        if (theChartTrack == null) {
            throw new TrackNotFoundException();
        }
        mChartTrackRepository.updateRank(theChartTrack, rank);
    }

    @Transactional
    @RequestMapping(value = "/chartTrack/updateLastWeekRanks", method = RequestMethod.POST)
    public void updateLastWeekRanks(@RequestParam(name = "password") String password,
            @RequestParam(name = "chartListId") Long chartListId) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        List<ChartTrack> theChartListTracks =
                mChartTrackRepository.findByChartList(mChartListRepository.findById(chartListId).orElse(null));
        for (ChartTrack ct : theChartListTracks) {
            List<Integer> thePreviousWeekRanks = mChartTrackRepository.findPreviousWeekRank(ct.getId());
            final int theLast = Ex.isEmpty(thePreviousWeekRanks) ? 0 : thePreviousWeekRanks.get(0);
            mChartTrackRepository.updateLastWeekRank(ct, theLast);
        }

    }

    @Transactional
    @RequestMapping(value = "/chartTrack/updateMissingTrack", method = RequestMethod.POST)
    public void addMissingChartTrack(@RequestParam(name = "password") String password,
            @RequestParam(name = "clId") Long chartListId, @RequestParam(name = "tId") Long trackId,
            @RequestParam(name = "rank") int rank) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        final Track theTrack = mTrackRepository.findById(trackId).orElse(null);
        if (theTrack == null) {
            throw new TrackNotFoundException();
        }
        final ChartList theChartList = mChartListRepository.findById(chartListId).orElse(null);
        if (theChartList == null) {
            throw new ChartListNotFoundException();
        }
    }

    ChartTrack getDebut(Track track) {
        return mChartTrackRepository.findByTrackAndSort(track, Sort.by("w.date")).get(0);
    }

    ChartTrack addMissingTrackInternal(ChartList chartList, Track track, int rank) {
        int theLastWeekRank = 0;
        final ChartList thePreviousChartList = mChartListRepository.findById(chartList.getPreviousChartListId()).orElse(null);
        if (thePreviousChartList != null) {
            final ChartTrack thePrevious = mChartTrackRepository.findByTrackAndChartList(track, thePreviousChartList);
            if (thePrevious != null) {
                theLastWeekRank = thePrevious.getRank();
            }
        }
        final ChartTrack theMissingChartTrack = new ChartTrack();
        theMissingChartTrack.setLastWeekRank(theLastWeekRank);
        theMissingChartTrack.setChartList(chartList);
        theMissingChartTrack.setTrack(track);
        theMissingChartTrack.setRank(rank);
        mChartTrackRepository.save(theMissingChartTrack);
        return theMissingChartTrack;
    }
}
