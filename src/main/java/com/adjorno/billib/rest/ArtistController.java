package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.*;
import com.adjorno.billib.rest.model.ArtistInfo;
import com.m14n.ex.Ex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ArtistController {
    @Autowired
    private ArtistRepository mArtistRepository;

    @Autowired
    private DuplicateArtistRepository mDuplicateArtistRepository;

    @Autowired
    private ArtistRelationRepository mArtistRelationRepository;

    @Autowired
    private GlobalRankArtistRepository mGlobalRankArtistRepository;

    @Autowired
    private TrackController mTrackController;

    @Transactional
    @RequestMapping(value = "/artist/getById", method = RequestMethod.GET)
    public Artist getById(@RequestParam() Long id) {
        Artist theArtist = mArtistRepository.findOne(id);
        if (theArtist == null) {
            throw new ArtistNotFoundException();
        }
        return theArtist;
    }

    @Transactional
    @RequestMapping(value = "/artist/info", method = RequestMethod.GET)
    public ArtistInfo getInfo(@RequestParam() Long id,
            @RequestParam(name = "relations_size", required = false, defaultValue = "5") int relationsSize,
            @RequestParam(name = "tracks_size", required = false, defaultValue = "5") int tracksSize) {
        Artist theArtist = mArtistRepository.findOne(id);
        if (theArtist == null) {
            throw new ArtistNotFoundException();
        }
        ArtistInfo theInfo = new ArtistInfo();
        theInfo.setArtist(theArtist);
        theInfo.setGlobalRank(mGlobalRankArtistRepository.findBymArtistId(theArtist.getId()).getRank());
        theInfo.setArtistRelations(getRelations(id, relationsSize));
        theInfo.setTracks(mTrackController.getTracks(theArtist, tracksSize));
        return theInfo;
    }

    @RequestMapping(value = "/artist/global", method = RequestMethod.GET)
    public List<Artist> getGlobalArtists(@RequestParam() Long rank,
            @RequestParam(required = false, defaultValue = "1") Long size) {
        return mArtistRepository.findGlobalList(rank, rank + size);
    }

    @Transactional
    @RequestMapping(value = "/artist/relations", method = RequestMethod.GET)
    public List<Artist> getRelations(@RequestParam() Long id,
            @RequestParam(required = false, defaultValue = "0") int size) {
        Artist theArtist = mArtistRepository.findOne(id);
        if (theArtist == null) {
            throw new ArtistNotFoundException();
        }
        List<Artist> theResult = new ArrayList<>();
        List<Artist> theSingleArtists = ArtistUtils.asSingleArtists(mArtistRelationRepository.findBymBand(theArtist));
        int theSinglesSize = 0;
        if (Ex.isNotEmpty(theSingleArtists)) {
            theSinglesSize = size == 0 || size >= theSingleArtists.size() ? theSingleArtists.size() : size;
            theResult.addAll(mArtistRepository
                    .sortByGlobalRank(ArtistUtils.asArtistIds(theSingleArtists), theSinglesSize));
        }
        if (size == 0 || size > theSinglesSize) {
            List<Artist> theBandArtists = ArtistUtils.asBandArtists(mArtistRelationRepository.findBymSingle(theArtist));
            if (Ex.isNotEmpty(theBandArtists)) {
                int theBandsSize = size == 0 || size - theSinglesSize >= theBandArtists.size() ? theBandArtists.size()
                        : size - theSinglesSize;
                theResult.addAll(mArtistRepository
                        .sortByGlobalRank(ArtistUtils.asArtistIds(theBandArtists), theBandsSize));
            }
        }
        return theResult;
    }

    @Transactional
    @RequestMapping(value = "/artist/rename", method = RequestMethod.POST)
    public void rename(@RequestParam() Long id, @RequestParam(name = "name") String newName) {
        Artist theArtist = mArtistRepository.findOne(id);
        if (theArtist == null) {
            throw new ArtistNotFoundException();
        }
        DuplicateArtist theDuplicate = mDuplicateArtistRepository.findBymDuplicateName(newName);
        String theOldName = theArtist.getName();
        if (theDuplicate != null) {
            if (!theDuplicate.getArtist().getId().equals(id)) {
                System.out.println("WARNING! Did not rename (" + theOldName + ") => (" + newName + ")");
                return;
            }
            mDuplicateArtistRepository.delete(theDuplicate);
            System.out.println("DUPLICATE REMOVED!");
        }
        mArtistRepository.rename(theArtist, newName);
        mDuplicateArtistRepository.save(new DuplicateArtist(theOldName, theArtist));
        System.out.println("RENAMED! " + theOldName + " => " + newName);
    }

    Artist findArtist(String artistName) {
        Artist theArtist = mArtistRepository.findBymName(artistName);
        if (theArtist == null) {
            final DuplicateArtist theDuplicate = mDuplicateArtistRepository.findBymDuplicateName(artistName);
            if (theDuplicate != null) {
                System.out
                        .println("FOUND DUPLICATE Artist: " + artistName + " => " + theDuplicate.getArtist().getName());
                theArtist = theDuplicate.getArtist();
            }
        }
        return theArtist;
    }
}
