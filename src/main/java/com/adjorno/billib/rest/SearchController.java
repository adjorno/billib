package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.Artist;
import com.adjorno.billib.rest.db.ArtistUtils;
import com.adjorno.billib.rest.db.Track;
import com.adjorno.billib.rest.db.TrackUtils;
import com.adjorno.billib.rest.model.MergedSearchResult;
import com.adjorno.billib.rest.model.SearchResult;
import com.m14n.ex.Ex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

@RestController
public class SearchController {
    private static final int ALL_RESULTS = -1;
    private static final int NO_RESULTS = 0;
    private static int MAX_RESULT_SIZE = 100;

    @Autowired
    private EntityManager mEntityManager;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public MergedSearchResult search(@RequestParam() String query,
            @RequestParam(name = "artists_offset", required = false, defaultValue = "0") int artistsOffset,
            @RequestParam(name = "artists_size", required = false, defaultValue = "5") int artistsSize,
            @RequestParam(name = "tracks_offset", required = false, defaultValue = "0") int tracksOffset,
            @RequestParam(name = "tracks_size", required = false, defaultValue = "10") int tracksSize,
            @RequestParam(name = "alphabetical", required = false, defaultValue = "false") boolean alphabetical) {
        MergedSearchResult theResult = new MergedSearchResult();
        String[] theKeywords = getKeywords(query);
        if (theKeywords.length > 0) {
            String theSearchQuery;
            if (artistsSize != NO_RESULTS) {
                SearchResult<Artist> theArtistSearchResult = new SearchResult<>();
                if (artistsSize > MAX_RESULT_SIZE || artistsSize == ALL_RESULTS) {
                    artistsSize = MAX_RESULT_SIZE;
                }
                theSearchQuery = ArtistUtils.getCountSearchQuery(theKeywords);
                int theTotal =
                        ((BigInteger) mEntityManager.createNativeQuery(theSearchQuery).getSingleResult()).intValue();
                List<Artist> theArtists = new ArrayList<>();
                if (artistsOffset < theTotal) {
                    theSearchQuery = ArtistUtils.getSearchQuery(theKeywords, artistsOffset, artistsSize, alphabetical);
                    theArtists = mEntityManager.createNativeQuery(theSearchQuery, Artist.class).getResultList();
                    if (theArtists.size() > MAX_RESULT_SIZE) {
                        theArtists = theArtists.subList(0, MAX_RESULT_SIZE);
                    }
                }
                theArtistSearchResult.setResults(theArtists);
                theArtistSearchResult.setTotal(theTotal);
                theArtistSearchResult.setOffset(artistsOffset);
                theResult.setArtists(theArtistSearchResult);

            }
            if (tracksSize != NO_RESULTS) {
                SearchResult<Track> theTrackSearchResult = new SearchResult<>();
                theSearchQuery = TrackUtils.getCountSearchQuery(theKeywords);
                if (tracksSize > MAX_RESULT_SIZE || tracksSize == ALL_RESULTS) {
                    tracksSize = MAX_RESULT_SIZE;
                }
                int theTotal = ((BigInteger) mEntityManager.createNativeQuery(theSearchQuery).getSingleResult())
                        .intValue();
                List<Track> theTracks = new ArrayList<>();
                if (artistsOffset < theTotal) {
                    theSearchQuery = TrackUtils.getSearchQuery(theKeywords, tracksOffset, tracksSize, alphabetical);
                    theTracks = mEntityManager.createNativeQuery(theSearchQuery, Track.class).getResultList();
                    if (theTracks.size() > MAX_RESULT_SIZE) {
                        theTracks = theTracks.subList(0, MAX_RESULT_SIZE);
                    }
                }
                theTrackSearchResult.setResults(theTracks);
                theTrackSearchResult.setTotal(theTotal);
                theTrackSearchResult.setOffset(tracksOffset);
                theResult.setTracks(theTrackSearchResult);
            }

        }
        return theResult;
    }

    private static String[] getKeywords(String query) {
        final String[] theSplits = Ex.isEmpty(query) ? new String[0] : query.split("(\\)|\\])?(\\s+|^|$)(\\(|\\[)?");
        final List<String> theKeywords = new ArrayList<>();
        for (String theSplit : theSplits) {
            if (isKeyword(theSplit)) {
                theKeywords.add(theSplit);
            }
        }
        return theKeywords.toArray(new String[0]);
    }

    private static boolean isKeyword(String split) {
        return split.matches(".*\\w+.*") & !split.matches(
                "(?i)" + "(and)" + "|(vs\\.?)" + "|(feat\\.?)" + "|(featuring)" + "|(presents)" + "|(^pres\\.?$)" +
                        "|(introducing)" + "|(starr?ing)" + "|(y)");
    }
}
