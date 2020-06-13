package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.*;
import com.adjorno.billib.rest.model.TrackInfo;
import com.m14n.ex.Ex;
import com.m14n.billib.data.BB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.*;

@RestController
public class TrackController implements ITrackController {
    private static final String PASSWORD = "vtldtlm";

    @Autowired
    private ChartRepository mChartRepository;

    @Autowired
    private TrackRepository mTrackRepository;

    @Autowired
    private ChartTrackRepository mChartTrackRepository;

    @Autowired
    private EntityManager mEntityManager;

    @Autowired
    private TrackCoverRepository mTrackCoverRepository;

    @Autowired
    private SpotifyUrlRepository mSpotifyUrlRepository;

    @Autowired
    private DayTrackRepository mDayTrackRepository;

    @Autowired
    private ArtistRepository mArtistRepository;

    @Autowired
    private GlobalRankTrackRepository mGlobalRankTrackRepository;


    @RequestMapping(value = "/track/getById", method = RequestMethod.GET)
    public Track track(@RequestParam(value = "id") Long id) {
        Track theOne = mTrackRepository.findOne(id);
        if (theOne == null) {
            throw new TrackNotFoundException();
        }
        TrackUtils.updateTrackExt(theOne, mTrackCoverRepository, mSpotifyUrlRepository);
        return theOne;
    }

    @RequestMapping(value = "/track/getByArtist", method = RequestMethod.GET)
    public List<Track> getTracksAPI(@RequestParam(name = "artist_id") Long artistId,
            @RequestParam(required = false, defaultValue = "0") int size) {
        Artist theArtist = mArtistRepository.findOne(artistId);
        if (theArtist == null) {
            throw new ArtistNotFoundException();
        }
        return getTracks(theArtist, size);
    }

    @Override
    public List<Track> getTracks(Artist artist, int size) {
        List<Track> theTracks = mTrackRepository.findByArtist(artist);
        List<Track> theResult = mTrackRepository
                .sortByGlobalRank(TrackUtils.asTrackIds(theTracks), size == 0 ? theTracks.size() : size);
        TrackUtils.updateTracksExt(theResult, mTrackCoverRepository, mSpotifyUrlRepository);
        return theResult;
    }

    @RequestMapping(value = "track/best", method = RequestMethod.GET)
    public Iterable<Track> bestTracks(@RequestParam(name = "chart_id") Long chartId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING)
                    String from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING)
                    String to, @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        final List<Track> theBestTracks =
                mEntityManager.createNativeQuery(TrackUtils.getBestTracksQuery(chartId, size, from, to), Track.class)
                        .getResultList();
        TrackUtils.updateTracksExt(theBestTracks, mTrackCoverRepository, mSpotifyUrlRepository);
        return theBestTracks;
    }

    @RequestMapping(value = "/track/day", method = RequestMethod.GET)
    public DayTrack dayTrack(
            @RequestParam(required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) String date) {
        final DayTrack theOne = Ex.isNotEmpty(date) ? mDayTrackRepository.findOne(java.sql.Date.valueOf(date))
                : mDayTrackRepository.findLast(new PageRequest(0, 1)).getContent().get(0);
        if (theOne != null) {
            TrackUtils.updateTrackExt(theOne.getTrack(), mTrackCoverRepository, mSpotifyUrlRepository);
        } else {
            throw new TrackNotFoundException();
        }
        return theOne;
    }

    @Transactional
    @RequestMapping(value = "/track/day", method = RequestMethod.POST)
    public void dayTrack(@RequestParam(name = "password") String password,
            @RequestParam() @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) String date) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        updateDayTrack(date);
    }

    @RequestMapping(value = "/track/history", method = RequestMethod.GET)
    public Map<String, Map<String, Integer>> getTrackHistoryAPI(@RequestParam() Long id,
            @RequestParam(required = false, name = "chart_id") Long chartId) {
        return getTrackHistory(id, chartId);
    }

    @Override
    public Map<String, Map<String, Integer>> getTrackHistory(Long id, Long chartId) {
        Track theTrack = mTrackRepository.findOne(id);
        if (theTrack == null) {
            throw new TrackNotFoundException();
        }
        Chart theRequestedChart = Ex.isPositive(chartId) ? mChartRepository.findOne(chartId) : null;
        Iterable<Chart> theCharts =
                theRequestedChart == null ? mChartRepository.findAll() : Arrays.asList(theRequestedChart);
        Map<String, Map<String, Integer>> theFullHistory = new HashMap<>();
        List<ChartTrack> theChartTracks = mChartTrackRepository.findByTrackInCharts(theTrack, theCharts);
        for (ChartTrack theChartTrack : theChartTracks) {
            String theChartName = theChartTrack.getChartList().getChart().getName();
            Map<String, Integer> theChartHistory = theFullHistory.get(theChartName);
            if (theChartHistory == null) {
                theChartHistory = new TreeMap<>();
                theFullHistory.put(theChartName, theChartHistory);
            }
            theChartHistory.put(theChartTrack.getChartList().getWeek().getDate(), theChartTrack.getRank());
        }
        return theFullHistory;
    }

    @RequestMapping(value = "/track/info", method = RequestMethod.GET)
    public TrackInfo getTrackInfo(@RequestParam() Long id) {
        Track theTrack = mTrackRepository.findOne(id);
        if (theTrack == null) {
            throw new TrackNotFoundException();
        }
        TrackUtils.updateTrackExt(theTrack, mTrackCoverRepository, mSpotifyUrlRepository);
        TrackInfo theTrackInfo = new TrackInfo();
        theTrackInfo.setTrack(theTrack);
        theTrackInfo.setHistory(getTrackHistory(id, null));
        theTrackInfo.setGlobalRank(mGlobalRankTrackRepository.findByTrackId(theTrack.getId()).getRank());
        return theTrackInfo;
    }

    @RequestMapping(value = "/track/global", method = RequestMethod.GET)
    public List<Track> getGlobalTracks(@RequestParam() Long rank,
            @RequestParam(required = false, defaultValue = "1") Long size) {
        List<Track> theGlobalTracks = mTrackRepository.findGlobalList(rank, rank + size);
        TrackUtils.updateTracksExt(theGlobalTracks, mTrackCoverRepository, mSpotifyUrlRepository);
        return theGlobalTracks;
    }

    @Override
    public Track updateDayTrack(String formattedDay) {
        final List<Track> theTracksOfTheDay = getTracksOfTheDay(formattedDay, 10);
        Track theTrack = theTracksOfTheDay.get(0);
        mDayTrackRepository.save(new DayTrack(java.sql.Date.valueOf(formattedDay), theTrack, null));
        return theTrack;
    }

    private List<Track> getTracksOfTheDay(String date, int size) {
        final List<Long> theTrackIds = mTrackRepository.findDebutsOfTheDay(date.substring(5));
        return mTrackRepository.sortByGlobalRank(theTrackIds, size);
    }
}
