package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m14n.billib.data.BB;
import com.m14n.billib.data.html.BBHtmlParser;
import com.m14n.billib.data.model.*;
import com.m14n.ex.Ex;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.io.*;
import java.text.ParseException;
import java.util.*;


@RestController
@ConditionalOnProperty(
    prefix = "update",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class UpdateController implements IUpdateController {

    private static final String PASSWORD = "vtldtlm";

    @Autowired
    private EntityManager mEntityManager;

    @Autowired
    private ChartTrackRepository mChartTrackRepository;

    @Autowired
    private ChartListRepository mChartListRepository;

    @Autowired
    private WeekRepository mWeekRepository;

    @Autowired
    private ArtistRepository mArtistRepository;

    @Autowired
    private TrackRepository mTrackRepository;

    @Autowired
    private ChartRepository mChartRepository;

    @Autowired
    private GlobalRankTrackRepository mGlobalRankTrackRepository;

    @Autowired
    private GlobalRankArtistRepository mGlobalRankArtistRepository;

    @Autowired
    private JournalRepository mJournalRepository;

    @Autowired
    private DuplicateArtistRepository mDuplicateArtistRepository;

    @Autowired
    private DuplicateTrackRepository mDuplicateTrackRepository;

    @Autowired
    private DuplicateController mDuplicateController;

    @Autowired
    private ArtistRelationRepository mArtistRelationRepository;

    @Autowired
    private ArtistController mArtistController;

    @Autowired
    private ApplicationEventPublisher mApplicationEventPublisher;

    @Value("${update.rawJsonData.path}")
    private String rawJsonDataPath;

    @Value("${update.today}")
    private String today;

    @Transactional
    @RequestMapping(value = "/update/relations", method = RequestMethod.POST)
    public void updateRelationAPI(@RequestParam(name = "password") String password,
                                  @RequestParam(defaultValue = "0") int from, @RequestParam(required = false, defaultValue = "100") int size,
                                  @RequestParam(name = "ignore") List<Long> ignore, @RequestParam() boolean dryRun) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        System.out.println("STARTED");
        List<Artist> theArtists = (List<Artist>) mArtistRepository.findAll();
        List<String[]> theSplits = new ArrayList<>();
        for (Artist theArtist : theArtists) {
            theSplits.add(ArtistUtils.splitCollaboration(theArtist.getName()));
        }
        System.out.println("SPLITS ARE BUILT");
        for (int i = 0; i < theArtists.size(); i++) {
            Artist theSingleArtist = theArtists.get(i);
            if (theSingleArtist.getId() < from) {
                continue;
            }
            if (ignore.indexOf(theSingleArtist.getId()) >= 0) {
                System.out.println("IGNORED " + theSingleArtist.getId());
                continue;
            }
            String theSingleArtistName = theSingleArtist.getName().toLowerCase();
            String[] theSingleSplits = theSplits.get(i);

            for (int j = 0; j < i; j++) {
                Artist theBandArtist = theArtists.get(j);
                String theBandArtistName = theBandArtist.getName().toLowerCase();
                String[] theBandSplits = theSplits.get(j);

                boolean contains = theBandArtistName.startsWith(theSingleArtistName + " ") ||
                        theBandArtistName.startsWith(theSingleArtistName + ",") ||
                        theBandArtistName.endsWith(" " + theSingleArtistName) ||
                        theBandArtistName.contains(" " + theSingleArtistName + " ") ||
                        theBandArtistName.contains(" " + theSingleArtistName + ",");
                if (contains) {
                    System.out.println(String.format("RELATION DIRECT! %d => %d %s => %s", theSingleArtist.getId(),
                            theBandArtist.getId(), theSingleArtist.getName(), theBandArtist.getName()));
                    if (!dryRun) {
                        mArtistRelationRepository.save(new ArtistRelation(theSingleArtist, theBandArtist));
                    }
                } else {
                    boolean found = true;
                    for (int k = 0; k < theSingleSplits.length; k++) {
                        boolean same = false;
                        for (int l = 0; l < theBandSplits.length; l++) {
                            if (ArtistUtils.equalsEasy(theSingleSplits[k], theBandSplits[l], mArtistRepository,
                                    mDuplicateArtistRepository)) {
                                same = true;
                                break;
                            }
                        }
                        if (same == false) {
                            found = false;
                            break;
                        }
                    }
                    if (found == true) {
                        System.out.println(
                                String.format("RELATION INDIRECT! %d => %d %s => %s", theSingleArtist.getId(),
                                        theBandArtist.getId(), theSingleArtist.getName(), theBandArtist.getName()));
                        if (!dryRun) {
                            mArtistRelationRepository.save(new ArtistRelation(theSingleArtist, theBandArtist));
                        }
                    }
                }
            }

            if (i % 100 == 0) {
                System.out.println("CHECKED " + i);
            }
            if (theSingleArtist.getId() - from >= size) {
                break;
            }
        }
        System.out.println("FINISHED");
    }

    @Transactional
    @RequestMapping(value = "/update/fixChartList", method = RequestMethod.GET)
    public List<ChartTrack> fixChartListAPI(@RequestParam(name = "password") String password,
                                            @RequestParam(name = "chartListId") Long chartListId) {
        if (!PASSWORD.equals(password)) {
            return null;
        }
        ChartList theChartList = mChartListRepository.findById(chartListId).orElse(null);
        if (theChartList == null) {
            throw new ChartListNotFoundException();
        }
        try {
            final BBJournalMetadata theJournal = new GsonBuilder().create()
                    .fromJson(new FileReader(new File(getClass().getResource("/metadata_billboard.json").getFile())),
                            BBJournalMetadata.class);
            for (BBChartMetadata theChartMetadata : theJournal.getCharts()) {
                if (theChartMetadata.getName().equals(theChartList.getChart().getName())) {
                    Document theChartDocument = BBHtmlParser
                            .getChartDocument(theJournal, theChartMetadata, theChartList.getWeek().getDate());
                    List<BBTrack> theTracks = BBHtmlParser.getTracks(theChartDocument);
                    if (theTracks.size() == theChartList.getChart().getListSize()) {
                        List<ChartTrack> theOldTracks = mChartTrackRepository.findByChartList(theChartList);
                        System.out.println("CLEAR OLD " + theOldTracks.size() + " TRACKS");
                        mChartTrackRepository.deleteAll(theOldTracks);
                        fillChartList(theChartList, theTracks);
                        mDuplicateController.checkLastWeek(theChartList.getId(), 1);
                        return mChartTrackRepository.findByChartList(theChartList);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Transactional
    @RequestMapping(value = "/insertMissingChartList", method = RequestMethod.GET)
    public ChartList insertMissingChartList(@RequestParam(name = "password") String password,
                                            long chartId, String date) {
        if (!PASSWORD.equals(password)) {
            return null;
        }
        Chart theChart = mChartRepository.findById(chartId).orElse(null);
        if (theChart == null) {
            return null;
        }
        Week theWeek = mWeekRepository.findByDate(date);
        if (theWeek == null) {
            return null;
        }
        ChartList theChartList = mChartListRepository.findByChartAndWeek(theChart, theWeek);
        if (theChartList != null) {
            return null;
        }
        List<ChartList> theAfter = mChartListRepository.findAfter(theChart, theWeek.getDate());
        ChartList theLastBefore = Ex.isNotEmpty(theAfter)
                ? mChartListRepository.findById(theAfter.get(0).getPreviousChartListId()).orElse(null)
                : null;
        ChartList theMissingChartList = getOrCreateChartList(theChart, theWeek, theLastBefore);
        for (int i = 0; i < theAfter.size(); i++) {
            final ChartList theAfterChartList = theAfter.get(i);
            if (i == 0) {
                mChartListRepository.updatePreviousId(theAfterChartList, theMissingChartList.getId());
            }
            mChartListRepository.updateNumber(theAfterChartList, theAfterChartList.getNumber() + 1);
        }
        return theMissingChartList;
    }

    @Transactional
    @RequestMapping(value = "/updateDB", method = RequestMethod.GET)
    public UpdateResult updateDBAPI(@RequestParam(name = "password") String password) {
        if (!PASSWORD.equals(password)) {
            return null;
        }
        return updateDB();
    }

    @RequestMapping(
            value = "/addChartFromLocal",
            method = RequestMethod.GET
    )
    public void addChartFromLocal(
            @RequestParam(name = "password") String password,
            @RequestParam(required = false) String chart,
            @RequestParam(required = false) String start,
            @RequestParam(required = false, defaultValue = "false") boolean skipMissingFiles) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        try {
            Gson theGson = new GsonBuilder().create();
            Reader theReader = new InputStreamReader(getClass().getResourceAsStream("/metadata_billboard.json"));
            final BBJournalMetadata theJournalMetadata =
                    theGson.fromJson(theReader, BBJournalMetadata.class);
            theReader.close();
            if (theJournalMetadata == null) {
                return;
            }
            Journal theJournal = getOrCreateJournal(theJournalMetadata.getName());
            final File localRoot = new File(rawJsonDataPath);
            for (BBChartMetadata theBBChartMetadata : theJournalMetadata.getCharts()) {
                if (chart == null || chart.equals(theBBChartMetadata.getName())) {
                    Chart theChart = getOrCreateChart(theJournal, theBBChartMetadata);
                    ChartList theLastChartList = null;
                    Calendar theCalendar = Calendar.getInstance();
                    if (Ex.isEmpty(start)) {
                        theCalendar.setTime(BB.CHART_DATE_FORMAT.parse(theBBChartMetadata.getStartDate()));
                    } else {
                        theCalendar.setTime(BB.CHART_DATE_FORMAT.parse(start));
                        theLastChartList = mChartListRepository.findLast(
                                theChart, PageRequest.of(0, 1)).getContent().get(0);
                    }
                    final Date lastWeek = BB.CHART_DATE_FORMAT.parse(
                            theChart.getEndDate() != null ? theChart.getEndDate() : today
                    );
                    final File localChart = new File(localRoot, theBBChartMetadata.getFolder());
                    while (theCalendar.getTime().compareTo(lastWeek) <= 0) {
                        String theDate = BB.CHART_DATE_FORMAT.format(theCalendar.getTime());
                        File chartListFile = new File(localChart, theBBChartMetadata.getPrefix() + "-" + theDate + ".json");
                        if (chartListFile.exists()) {
                            theReader = new InputStreamReader(new FileInputStream(chartListFile));
                            try {
                                BBChart theBBChart = theGson.fromJson(theReader, BBChart.class);
                                final Week theWeek = getOrCreateWeek(theDate);
                                ChartList theChartList = getOrCreateChartList(theChart, theWeek, theLastChartList);
                                fillChartList(theChartList, theBBChart.getTracks());
                                theLastChartList = theChartList;
                            } finally {
                                theReader.close();
                            }
                        } else {
                            if (skipMissingFiles) {
                                System.out.println("!!! SKIPPED " + theBBChartMetadata.getName() + " " + theDate);
                            } else {
                                throw new MissingResourceException(
                                        "The file " + chartListFile.getName() + " with required chart list is missing!",
                                        "UpdateController",
                                        chartListFile.getName()
                                );
                            }
                        }
                        theCalendar.add(Calendar.DATE, 7);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public UpdateResult updateDB() {
        final Iterable<Chart> theCharts = mChartRepository.findAll();
        try {
            Reader theReader = new InputStreamReader(getClass().getResourceAsStream("/metadata_billboard.json"));
            final BBJournalMetadata theJournal =
                    new GsonBuilder().create().fromJson(theReader, BBJournalMetadata.class);
            if (theJournal == null) {
                return null;
            }
            theReader.close();
            final UpdateResult theUpdates = new UpdateResult();


            Date theHot100Update = null;
            Date theLastUpdate = null;
            for (Chart theChart : theCharts) {
                BBChartMetadata theChartMetadata = null;
                for (BBChartMetadata theMetadata : theJournal.getCharts()) {
                    if (theChart.getName().equals(theMetadata.getName())) {
                        theChartMetadata = theMetadata;
                        break;
                    }
                }
                Document theLastChartDocument = BBHtmlParser.getChartDocument(theJournal, theChartMetadata, null);
                Date theLastWeekDate = BBHtmlParser.getChartDate(theLastChartDocument);
                String theLastWeek = BB.CHART_DATE_FORMAT.format(theLastWeekDate);
                ChartList theLastChartList =
                        mChartListRepository.findLast(theChart, PageRequest.of(0, 1)).getContent().get(0);

                Calendar theCalendar = Calendar.getInstance();
                theCalendar.setTime(BB.CHART_DATE_FORMAT.parse(theLastChartList.getWeek().getDate()));
                while (theCalendar.getTime().before(theLastWeekDate)) {
                    String thePrevious = BB.CHART_DATE_FORMAT.format(theCalendar.getTime());
                    theCalendar.add(Calendar.DATE,
                            "2017-12-30".equals(thePrevious) ? 4 : "2018-01-03".equals(thePrevious) ? 3 : 7);
                    Date theRequestedDate = theCalendar.getTime();
                    String theRequestedDateString = BB.CHART_DATE_FORMAT.format(theRequestedDate);
                    final Week theWeek = getOrCreateWeek(theRequestedDateString);
                    ChartList theChartList = mChartListRepository.findByChartAndWeek(theChart, theWeek);
                    if (theChartList != null) {
                        System.out.println("The chart list " + theChartList.toString() + " is already exist!");
                        theLastChartList = theChartList;
                        continue;
                    }
                    Document theDocument = theRequestedDateString.equals(theLastWeek)
                            ? theLastChartDocument
                            : BBHtmlParser.getChartDocument(theJournal, theChartMetadata, theRequestedDateString);
                    List<BBTrack> theTracks = BBHtmlParser.getTracks(theDocument);
                    if (theRequestedDate.equals(BBHtmlParser.getChartDate(theDocument)) && Ex.isNotEmpty(theTracks)) {
                        theChartList = getOrCreateChartList(theChart, theWeek, theLastChartList);
                        fillChartList(theChartList, theTracks);
                        theLastChartList = theChartList;
                        theUpdates.addChartUpdate(theChart.getName() + " " + theRequestedDateString,
                                theChart.getListSize() == theTracks.size() ? UpdateResult.RESULT_OK
                                        : UpdateResult.RESULT_DIFFERENT_SIZE);
                        // check Hot-100 update
                        if (theLastUpdate == null || theLastUpdate.before(theRequestedDate)) {
                            theLastUpdate = theRequestedDate;
                        }
                        if (theChart.getId() == 1) {
                            theHot100Update = theRequestedDate;
                        }
                    } else {
                        duplicateChartList(theLastChartList, theChartList);
                        theUpdates.addChartUpdate(theChart.getName() + " " + theRequestedDateString,
                                UpdateResult.RESULT_DUPLICATE);
                    }
                }
            }
            theUpdates.setUpdateWeek(
                    theHot100Update != null && theHot100Update.compareTo(theLastUpdate) == 0 ? theHot100Update : null);
            updateGlobalRankingTrack();
            updateGlobalRankingArtist();

            mApplicationEventPublisher.publishEvent(theUpdates);
            return theUpdates;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Transactional
    @RequestMapping(value = "/sendUpdateDB", method = RequestMethod.GET)
    public boolean sendUpdateDBAPI(@RequestParam(name = "password") String password) {
        if (!PASSWORD.equals(password)) {
            return false;
        }
        UpdateResult theUpdateResult = new UpdateResult();
        theUpdateResult.setUpdateWeek(Calendar.getInstance().getTime());
        theUpdateResult.addChartUpdate("Hot 300", UpdateResult.RESULT_OK);
        mApplicationEventPublisher.publishEvent(theUpdateResult);
        return true;
    }

    private void duplicateChartList(ChartList from, ChartList to) {
        List<ChartTrack> theChartTracks = mChartTrackRepository.findByChartList(from);
        List<ChartTrack> theNewChartTracks = new ArrayList<>();
        for (ChartTrack theChartTrack : theChartTracks) {
            ChartTrack theNewChartTrack = new ChartTrack();
            theNewChartTrack.setTrack(theChartTrack.getTrack());
            theChartTrack.setRank(theChartTrack.getRank());
            theChartTrack.setLastWeekRank(theChartTrack.getRank());
            theChartTrack.setChartList(to);
            theNewChartTracks.add(theNewChartTrack);
        }
        mChartTrackRepository.saveAll(theNewChartTracks);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/updateGlobal", method = RequestMethod.POST)
    public void updateGlobalAPI(@RequestParam(name = "password") String password) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        updateGlobalRankingTrack();
        updateGlobalRankingArtist();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGlobalRankingTrack() {
        System.out.println("STARTED UPDATE GLOBAL TRACK");
        mEntityManager.createNativeQuery("TRUNCATE TABLE GLOBAL_RANK_TRACK").executeUpdate();
        mGlobalRankTrackRepository.refreshAll();
        System.out.println("FINISHED UPDATE GLOBAL TRACK");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGlobalRankingArtist() {
        System.out.println("STARTED UPDATE GLOBAL ARTIST");
        mEntityManager.createNativeQuery("TRUNCATE TABLE GLOBAL_RANK_ARTIST").executeUpdate();
        mGlobalRankArtistRepository.refreshAll();
        mGlobalRankArtistRepository.addMissing();
        System.out.println("FINISHED UPDATE GLOBAL ARTIST");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void fillChartList(ChartList chartList, List<BBTrack> tracks) {
        for (BBTrack theBBTrack : tracks) {
            final Artist theArtist = getOrCreateArtist(theBBTrack.getArtist().trim());
            final Track theTrack = getOrCreateTrack(theArtist, theBBTrack.getTitle().trim());
            final BBPositionInfo thePositionInfo = theBBTrack.getPositionInfo();
            final int theLastWeek =
                    BB.extractLastWeekRank(thePositionInfo == null ? null : thePositionInfo.getLastWeek());
            createChartTrack(chartList, theTrack, theBBTrack.getRank(), theLastWeek);
        }
    }

    private Chart getOrCreateChart(Journal journal, BBChartMetadata theChartMetadata) {
        Chart theChart = mChartRepository.findByName(theChartMetadata.getName());
        if (theChart == null) {
            theChart = new Chart();
            theChart.setJournal(journal);
            theChart.setName(theChartMetadata.getName());
            theChart.setListSize(theChartMetadata.getSize());
            theChart.setStartDate(theChartMetadata.getStartDate());
            mChartRepository.save(theChart);
            System.out.println("CREATED Chart " + theChart.getName());
        }
        return theChart;
    }

    private Journal getOrCreateJournal(String journalName) {
        Journal theJournal = mJournalRepository.findByName(journalName);
        if (theJournal == null) {
            theJournal = new Journal();
            theJournal.setName(journalName);
            mJournalRepository.save(theJournal);
            System.out.println("CREATED Journal " + journalName);
        }
        return theJournal;
    }

    private ChartList getOrCreateChartList(Chart chart, Week week, ChartList previousChartList) {
        ChartList theNewChartList = mChartListRepository.findByChartAndWeek(chart, week);
        if (theNewChartList == null) {
            final ChartList theLastChartList;
            if (previousChartList != null) {
                theLastChartList = previousChartList;
            } else {
                List<ChartList> theLast = mChartListRepository.findLast(chart, PageRequest.of(0, 1)).getContent();
                theLastChartList = Ex.isNotEmpty(theLast) ? theLast.get(0) : null;
            }
            theNewChartList = new ChartList();
            theNewChartList.setChart(chart);
            theNewChartList.setWeek(week);
            theNewChartList.setNumber(theLastChartList == null ? 1 : theLastChartList.getNumber() + 1);
            theNewChartList.setPreviousChartListId(theLastChartList == null ? 0 : theLastChartList.getId());
            mChartListRepository.save(theNewChartList);
            System.out.println("CREATED ChartList " + theNewChartList.toString() + ". ID = " + theNewChartList.getId());
        }
        return theNewChartList;
    }

    private Week getOrCreateWeek(String chartDate) {
        Week theWeek = mWeekRepository.findByDate(chartDate);
        if (theWeek == null) {
            theWeek = new Week();
            theWeek.setDate(chartDate);
            mWeekRepository.save(theWeek);
        }
        return theWeek;
    }

    private Artist getOrCreateArtist(String rawArtistName) {
        String artistName = ArtistUtils.optimizeName(rawArtistName);
        Artist theArtist = mArtistController.findArtist(artistName);
        if (theArtist == null) {
            theArtist = new Artist();
            theArtist.setName(artistName);
            mArtistRepository.save(theArtist);
            System.out.println("CREATED Artist: " + theArtist.toString() + ". ID = " + theArtist.getId());
            updateRelationsForArtist(theArtist);
        }
        return theArtist;
    }

    private void updateRelationsForArtist(Artist artist) {
        String[] splits = ArtistUtils.splitCollaboration(artist.getName());
        List<ArtistRelation> theArtistRelations = new ArrayList<>();
        if (splits.length > 1) {
            for (String split : splits) {
                Artist theArtist = mArtistController.findArtist(split);
                if (theArtist != null && theArtist.getId() != artist.getId()) {
                    theArtistRelations.add(new ArtistRelation(theArtist, artist));
                }
            }
        }
        List<Artist> theLikeArtists = mArtistRepository.findByNameLike(artist.getName());
        for (Artist theLikeArtist : theLikeArtists) {
            if (theLikeArtist.getId() != artist.getId()) {
                theArtistRelations.add(new ArtistRelation(artist, theLikeArtist));
            }
        }
        mArtistRelationRepository.saveAll(theArtistRelations);
        for (ArtistRelation theArtistRelation : theArtistRelations) {
            System.out.println(
                    String.format("Created ArtistRelation: %d => %d, %s => %s", theArtistRelation.getSingle().getId(),
                            theArtistRelation.getBand().getId(), theArtistRelation.getSingle().getName(),
                            theArtistRelation.getBand().getName()));
        }
    }

    private Track getOrCreateTrack(Artist artist, String trackTitle) {
        Track theTrack = mTrackRepository.findByTitleAndArtist(trackTitle, artist);
        if (theTrack == null) {
            final DuplicateTrack theDuplicate = mDuplicateTrackRepository.
                    findByDuplicateTitle(artist.generateDuplicateTitle(trackTitle));
            if (theDuplicate != null) {
                System.out.println(
                        "FOUND DUPLICATE Track: " + artist.generateDuplicateTitle(trackTitle) + " => " +
                                theDuplicate.getTrack().toString());
                theTrack = theDuplicate.getTrack();
            }
        }
        if (theTrack == null) {
            theTrack = new Track();
            theTrack.setArtist(artist);
            theTrack.setTitle(trackTitle);
            mTrackRepository.save(theTrack);
            System.out.println("CREATED Track: " + theTrack.toString() + ". ID = " + theTrack.getId());
        }
        return theTrack;
    }

    private ChartTrack createChartTrack(ChartList chartList, Track track, int rank, int lastWeek) {
        final ChartTrack theChartTrack = new ChartTrack();
        theChartTrack.setChartList(chartList);
        theChartTrack.setTrack(track);
        theChartTrack.setRank(rank);
        theChartTrack.setLastWeekRank(lastWeek);
        mChartTrackRepository.save(theChartTrack);
        return theChartTrack;
    }

}
