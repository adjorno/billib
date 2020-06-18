package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.*;
import com.adjorno.billib.rest.model.MergeOperation;
import com.m14n.ex.Ex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class DuplicateController implements IDuplicateController {

    public static final String PASSWORD = "vtldtlm";

    @Autowired
    private ChartListRepository mChartListRepository;

    @Autowired
    private ArtistRepository mArtistRepository;

    @Autowired
    private TrackRepository mTrackRepository;

    @Autowired
    private DuplicateArtistRepository mDuplicateArtistRepository;

    @Autowired
    private DuplicateTrackRepository mDuplicateTrackRepository;

    @Autowired
    private ChartTrackRepository mChartTrackRepository;

    @Autowired
    private DayTrackRepository mDayTrackRepository;

    @Autowired
    private TrendTrackRepository mTrendTrackRepository;

    @Autowired
    private ChartTrackController mChartTrackController;

    @Autowired
    private ArtistRelationRepository mArtistRelationRepository;

    @Transactional
    @RequestMapping(value = "/duplicate/checkTracks", method = RequestMethod.POST)
    public void checkTracksAPI(@RequestParam(name = "password") String password,
            @RequestParam(name = "fromArtist", defaultValue = "1") int from,
            @RequestParam(name = "checkSize", defaultValue = "100") int size) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        List<Artist> theArtists = (List<Artist>) mArtistRepository.findAll();
        Map<Track, Track> theLeftDuplicates = new HashMap<>();
        for (int a = from; a < theArtists.size(); a++) {
            List<Track> theTracks = mTrackRepository.findByArtist(theArtists.get(a));
            for (int t1 = 0; t1 < theTracks.size(); t1++) {
                Track theTrack1 = theTracks.get(t1);
                for (int t2 = t1 + 1; t2 < theTracks.size(); t2++) {
                    Track theTrack2 = theTracks.get(t2);
                    if (!theTrack1.getId().equals(theTrack2.getId())) {
                        if (Ex.equalsValued(theTrack1.getTitle(), theTrack2.getTitle())) {
                            int l1 = Ex.getValuedLength(theTrack1.getTitle());
                            int l2 = Ex.getValuedLength(theTrack2.getTitle());
                            if (l1 > l2) {
                                removeDuplicateTrack(theTrack1.getId(), theTrack2.getId());
                                System.out.println(theTrack2.getTitle() + " => " + theTrack1.getTitle());
                            } else if (l2 > l1) {
                                removeDuplicateTrack(theTrack2.getId(), theTrack1.getId());
                                System.out.println(theTrack1.getTitle() + " => " + theTrack2.getTitle());
                            } else {
                                theLeftDuplicates.put(theTrack1, theTrack2);
                            }
                        }
                    }
                }
            }
            if (a - from > size) {
                break;
            }
        }
        System.out.println("FINISHED");
        System.out.println("POSSIBLE TRACK DUPLICATES:");
        for (Map.Entry<Track, Track> entry : theLeftDuplicates.entrySet()) {
            Track fromTrack = entry.getKey();
            Track toTrack = entry.getValue();
            System.out.println(
                    String.format("%d %d (%s => %s)", fromTrack.getId(), toTrack.getId(), fromTrack.getTitle(),
                            toTrack.getTitle()));
        }
    }

    @Transactional
    @RequestMapping(value = "/duplicate/checkArtists", method = RequestMethod.POST)
    public void checkArtistsAPI(@RequestParam(name = "password") String password,
            @RequestParam(name = "fromArtist", defaultValue = "1") int from,
            @RequestParam(name = "checkSize", defaultValue = "100") int size) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        List<Artist> theArtists = (List<Artist>) mArtistRepository.findAll();
        Map<Artist, Artist> theLeftDuplicates = new HashMap<>();
        for (int i = from; i < theArtists.size(); i++) {
            Artist theArtist1 = theArtists.get(i);
            for (int j = i + 1; j < theArtists.size(); j++) {
                Artist theArtist2 = theArtists.get(j);
                if (Ex.equalsValued(theArtist1.getName(), theArtist2.getName())) {
                    int l1 = Ex.getValuedLength(theArtist1.getName());
                    int l2 = Ex.getValuedLength(theArtist2.getName());
                    if (l1 > l2) {
                        removeDuplicateArtist(theArtist1.getId(), theArtist2.getId());
                        System.out.println(theArtist2.getName() + " => " + theArtist1.getName());
                    } else if (l2 > l1) {
                        removeDuplicateArtist(theArtist2.getId(), theArtist1.getId());
                        System.out.println(theArtist1.getName() + " => " + theArtist2.getName());

                    } else {
                        theLeftDuplicates.put(theArtist1, theArtist2);
                    }

                }
            }
            if (i - from > size) {
                break;
            }
        }
        System.out.println("FINISHED");
        System.out.println("POSSIBLE ARTIST DUPLICATES:");
        for (Map.Entry<Artist, Artist> entry : theLeftDuplicates.entrySet()) {
            Artist fromArtist = entry.getKey();
            Artist toArtist = entry.getValue();
            System.out.println(
                    String.format("%d %d (%s => %s)", fromArtist.getId(), toArtist.getId(), fromArtist.getName(),
                            toArtist.getName()));
        }
    }

    @Transactional
    @RequestMapping(value = "/duplicate/checkLastWeek", method = RequestMethod.POST)
    public void checkLastWeekAPI(@RequestParam(name = "password") String password,
            @RequestParam(name = "from", defaultValue = "1") long from,
            @RequestParam(name = "size", defaultValue = "500") long size) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        checkLastWeek(from, size);
    }

    @Transactional
    @RequestMapping(value = "/duplicate/artist", method = RequestMethod.GET)
    public List<MergeOperation> removeDuplicateArtistAPI(@RequestParam(name = "password") String password,
            @RequestParam(name = "originalArtistId") Long originalId,
            @RequestParam(name = "duplicateArtistId") Long duplicateId) {
        if (!PASSWORD.equals(password)) {
            return null;
        }
        List<MergeOperation> theMergeOperations = removeDuplicateArtist(originalId, duplicateId);
        if (Ex.isNotEmpty(theMergeOperations)) {
            return theMergeOperations;
        } else {
            throw new ArtistNotFoundException();
        }
    }

    @Transactional
    @RequestMapping(value = "/duplicate/track", method = RequestMethod.GET)
    public MergeOperation<Track> removeDuplicateTrackAPI(@RequestParam(name = "password") String password,
            @RequestParam(name = "originalTrackId") Long originalId,
            @RequestParam(name = "duplicateTrackId") Long duplicateId) {
        if (!PASSWORD.equals(password)) {
            return null;
        }
        MergeOperation<Track> theTrackMergeOperation = removeDuplicateTrack(originalId, duplicateId);
        if (theTrackMergeOperation != null) {
            return theTrackMergeOperation;
        } else {
            throw new TrackNotFoundException();
        }
    }

    @Transactional
    @RequestMapping(value = "/duplicate/checkCollaborations", method = RequestMethod.POST)
    public void removeDuplicateCollaborationsAPI(@RequestParam(name = "password") String password,
            @RequestParam(defaultValue = "0") int from, @RequestParam(required = false, defaultValue = "100") int size,
            @RequestParam() boolean dryRun) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        List<Artist> theArtists = (List<Artist>) mArtistRepository.findAll();
        System.out.println("STARTED");
        Set<String> fs = new HashSet<>();
        for (int i = from; i < theArtists.size(); i++) {
            Artist theArtist1 = theArtists.get(i);
            String[] a = ArtistUtils.splitCollaboration(theArtist1.getName());
            int a1l = a.length;
            if (a1l == 1) {
                continue;
            }
            Set<String> a1 = new HashSet<>();
            a1.addAll(Arrays.asList(a));
            for (int j = i + 1; j < theArtists.size(); j++) {
                Artist theArtist2 = theArtists.get(j);
                String[] b = ArtistUtils.splitCollaboration(theArtist2.getName());
                if (b.length != a1l) {
                    continue;
                }
                Set<String> b1 = new HashSet<>();
                b1.addAll(Arrays.asList(b));
                if (a1.equals(b1)) {
                    Artist toRemove = theArtist2;
                    Artist toMerge = theArtist1;
                    if (theArtist1.getName().contains("and") || theArtist1.getName().contains("And")) {
                        toRemove = theArtist1;
                        toMerge = theArtist2;
                    }

                    System.out.println(
                            String.format("%d => %d, %s => %s", toRemove.getId(), toMerge.getId(), toRemove.getName(),
                                    toMerge.getName()));
                    if (!dryRun) {
                        removeDuplicateArtist(toMerge.getId(), toRemove.getId());
                    }
                }
            }
            if (i % 100 == 0) {
                System.out.println("CHECKED " + i);
            }
            if (i - from >= size) {
                break;
            }
        }
        System.out.println("FINISHED");
        for (Object f : fs.toArray()) {
            System.out.println(f);
        }
    }

    @Override
    public void checkLastWeek(long from, long size) {
        final long theSize = mChartListRepository.count();
        ChartList thePreviousChartList = null;
        List<ChartTrack> thePreviousChartTracks = new ArrayList<>();
        Map<Artist, Artist> thePossibleArtistUpdates = new HashMap<>();
        final long start = from;
        System.out.println("CHECK STARTED");
        while (from <= theSize) {
            final ChartList theChartList = mChartListRepository.findOne(from);
            final List<ChartTrack> theChartTracks = mChartTrackRepository.findByChartList(theChartList);

            if (thePreviousChartList != null &&
                    !thePreviousChartList.getChart().getId().equals(theChartList.getChart().getId())) {
                thePreviousChartList = null;
                thePreviousChartTracks = null;
            }

            if (thePreviousChartList == null) {
                thePreviousChartList = mChartListRepository.findOne(theChartList.getPreviousChartListId());
            }
            if (Ex.isEmpty(thePreviousChartTracks) && thePreviousChartList != null) {
                thePreviousChartTracks = mChartTrackRepository.findByChartList(thePreviousChartList);
            }
            if (Ex.isNotEmpty(thePreviousChartTracks)) {
                for (ChartTrack theChartTrack : theChartTracks) {
                    final int theLastWeekRank = theChartTrack.getLastWeekRank();
                    if (theLastWeekRank != 0 && theLastWeekRank <= theChartList.getChart().getListSize()) {
                        boolean found = false;
                        List<ChartTrack> theSameRankTracks = new ArrayList<>();
                        for (ChartTrack thePreviousChartTrack : thePreviousChartTracks) {
                            if (thePreviousChartTrack.getRank() == theLastWeekRank) {
                                theSameRankTracks.add(thePreviousChartTrack);
                                if (theChartTrack.getTrack().getId().equals(thePreviousChartTrack.getTrack().getId())) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            if (Ex.isEmpty(theSameRankTracks)) {
                                // try to find the same track in previous list
                                ChartTrack theSameTrack = mChartTrackRepository
                                        .findByTrackAndChartList(theChartTrack.getTrack(), thePreviousChartList);
                                if (theSameTrack != null) {
                                    mChartTrackRepository.updateRank(theSameTrack, theLastWeekRank);
                                    mChartTrackRepository.updateLastWeekRank(theChartTrack, theSameTrack.getRank());
                                    System.out.println("FIXED! UPDATED LAST WEEK RANK");
                                    continue;
                                } else {
                                    if (mChartTrackRepository.countRealChartListSize(thePreviousChartList).size() <
                                            theChartList.getChart().getListSize()) {
                                        ChartTrack theMissingTrack = mChartTrackController.
                                                addMissingTrackInternal(thePreviousChartList, theChartTrack.getTrack(),
                                                        theLastWeekRank);
                                        System.out.println("FIXED! ADDED MISSING TRACK - " + theMissingTrack);
                                        continue;
                                    }
                                }
                            } else {
                                if (theSameRankTracks.size() == 1) {
                                    ChartTrack theDuplicate = theSameRankTracks.get(0);
                                    if (theDuplicate.getTrack().getArtist().getId()
                                            .equals(theChartTrack.getTrack().getArtist().getId())) {

                                        int l1 = Ex.getValuedLength(theDuplicate.getTrack().getTitle());
                                        int l2 = Ex.getValuedLength(theChartTrack.getTrack().getTitle());
                                        Track original = l1 >= l2 ? theDuplicate.getTrack() : theChartTrack.getTrack();
                                        Track duplicate = l1 >= l2 ? theChartTrack.getTrack() : theDuplicate.getTrack();
                                        MergeOperation<Track> theTrackMergeOperation =
                                                removeDuplicateTrack(original.getId(), duplicate.getId());
                                        if (theTrackMergeOperation != null) {
                                            theChartTrack.setTrack(theTrackMergeOperation.getMergedTo());
                                        } else {
                                            System.out.print("NOT ");
                                        }
                                        System.out.println(
                                                "FIXED! REMOVE DUPLICATE TRACK - " + duplicate.toString() + " " +
                                                        duplicate.getId() + " => " + original.toString() + " " +
                                                        original.getId());
                                        continue;
                                    } else {
                                        if (Ex.equalsValued(theDuplicate.getTrack().getTitle(),
                                                theChartTrack.getTrack().getTitle())) {
                                            int l1 = Ex.getValuedLength(theDuplicate.getTrack().getArtist().getName());
                                            int l2 = Ex.getValuedLength(theChartTrack.getTrack().getArtist().getName());
                                            Track original =
                                                    l1 >= l2 ? theDuplicate.getTrack() : theChartTrack.getTrack();
                                            Track duplicate =
                                                    l1 >= l2 ? theChartTrack.getTrack() : theDuplicate.getTrack();
                                            MergeOperation<Track> theTrackMergeOperation =
                                                    removeDuplicateTrack(original.getId(), duplicate.getId());
                                            if (theTrackMergeOperation != null) {
                                                theChartTrack.setTrack(theTrackMergeOperation.getMergedTo());
                                            } else {
                                                System.out.print("NOT ");
                                            }
                                            System.out.println(
                                                    "FIXED! REMOVE DUPLICATE TRACK - " + duplicate.toString() + " " +
                                                            duplicate.getId() + " => " + original.toString() + " " +
                                                            original.getId());
                                            thePossibleArtistUpdates.put(original.getArtist(), duplicate.getArtist());
                                            continue;
                                        }
                                    }
                                }
                            }
                            reportLastWeekRankProblem(thePreviousChartList, theChartList, theChartTrack,
                                    theSameRankTracks);
                        }
                    }
                }
            }
            if (++from % 500 == 0) {
                System.out.println("CHECK " + from);
            }
            if (from - start > size) {
                break;
            }
            thePreviousChartList = theChartList;
            thePreviousChartTracks = theChartTracks;
        }
        System.out.println("CHECK FINISHED");
        System.out.println("POSSIBLE ARTIST DUPLICATES:");
        for (Map.Entry<Artist, Artist> entry : thePossibleArtistUpdates.entrySet()) {
            Artist fromArtist = entry.getKey();
            Artist toArtist = entry.getValue();
            System.out.println(
                    String.format("%d %d (%s => %s)", fromArtist.getId(), toArtist.getId(), fromArtist.getName(),
                            toArtist.getName()));
        }
    }

    private List<MergeOperation> removeDuplicateArtist(Long originalId, Long duplicateId) {
        final Artist theOriginalArtist = mArtistRepository.findOne(originalId);
        final Artist theDuplicateArtist = mArtistRepository.findOne(duplicateId);
        if (theOriginalArtist != null && theDuplicateArtist != null) {
            List<MergeOperation> theMerges = new ArrayList<>();
            List<String> theRepeatTitles =
                    mTrackRepository.findRepeatTitles(theDuplicateArtist.getId(), theOriginalArtist.getId());
            for (String theTitle : theRepeatTitles) {
                MergeOperation<Track> theTrackMergeOperation = removeDuplicateTrack(
                        mTrackRepository.findByTitleAndArtist(theTitle, theOriginalArtist).getId(),
                        mTrackRepository.findByTitleAndArtist(theTitle, theDuplicateArtist).getId());
                if (theTrackMergeOperation == null) {
                    System.out.println("Could not merge the track - " + theTitle);
                } else {
                    theMerges.add(theTrackMergeOperation);
                }
            }
            mTrackRepository.updateArtists(theDuplicateArtist, theOriginalArtist);
            List<Long> mergingSingleIds = mArtistRelationRepository
                    .findMergingSingleIds(theDuplicateArtist.getId(), theOriginalArtist.getId());
            for (Number mergeSingleId : mergingSingleIds) {
                mArtistRelationRepository
                        .deleteBySingleIdAndBandId(mergeSingleId.longValue(), theDuplicateArtist.getId());
            }
            mArtistRelationRepository.updateSingleArtists(theDuplicateArtist, theOriginalArtist);

            List<Long> mergingBandIds =
                    mArtistRelationRepository.findMergingBandIds(theDuplicateArtist.getId(), theOriginalArtist.getId());
            for (Number mergingBandId : mergingBandIds) {
                mArtistRelationRepository
                        .deleteBySingleIdAndBandId(theDuplicateArtist.getId(), mergingBandId.longValue());
            }
            mArtistRelationRepository.updateBandArtists(theDuplicateArtist, theOriginalArtist);
            mDuplicateArtistRepository.updateArtists(theDuplicateArtist, theOriginalArtist);
            mDuplicateArtistRepository.save(new DuplicateArtist(theDuplicateArtist.getName(), theOriginalArtist));

            mArtistRepository.delete(theDuplicateArtist);
            theMerges.add(new MergeOperation(theDuplicateArtist, theOriginalArtist));
            return theMerges;
        }
        return null;
    }

    private MergeOperation<Track> removeDuplicateTrack(Long originalId, Long duplicateId) {
        Track duplicateTrack = mTrackRepository.findOne(duplicateId);
        Track originalTrack = mTrackRepository.findOne(originalId);
        if (originalTrack != null && duplicateTrack != null) {
            mChartTrackRepository.updateTracks(duplicateTrack, originalTrack);
            mDayTrackRepository.updateTracks(duplicateTrack, originalTrack);

            mTrendTrackRepository.updateTracks(duplicateTrack, originalTrack);
            mDuplicateTrackRepository.updateTracks(duplicateTrack, originalTrack);
            mDuplicateTrackRepository.save(new DuplicateTrack(
                    originalTrack.getArtist().generateDuplicateTitle(duplicateTrack.getTitle()),
                    originalTrack));

            mTrackRepository.delete(duplicateTrack);
            return new MergeOperation<>(duplicateTrack, originalTrack);
        }
        return null;
    }

    private void reportLastWeekRankProblem(ChartList previousChartList, ChartList chartList, ChartTrack theChartTrack,
                                           List<ChartTrack> sameRankTracks) {
        System.out.println("****************");
        System.out.println("--- ORIGINAL TRACK FROM CHART LIST " + chartList.getId());
        System.out.println(theChartTrack.toString() + " TRACK_ID = " + theChartTrack.getTrack().getId());
        System.out.println("--- POSSIBLE TRACKS FROM PREVIOUS CHART_LIST " + previousChartList.getId() + " AND RANK " +
                theChartTrack.getLastWeekRank());
        for (ChartTrack thePrevious : sameRankTracks) {
            System.out.println(thePrevious + " " + thePrevious.getTrack().getId());
        }
    }

}
