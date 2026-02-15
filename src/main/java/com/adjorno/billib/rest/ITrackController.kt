package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.Artist;
import com.adjorno.billib.rest.db.Track;

import java.util.List;
import java.util.Map;

public interface ITrackController {
    List<Track> getTracks(Artist artist, int size);

    Map<String, Map<String, Integer>> getTrackHistory(Long id, Long chartId);

    Track updateDayTrack(String formattedDay);
}
