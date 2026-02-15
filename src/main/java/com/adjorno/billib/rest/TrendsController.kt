package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.*;
import com.adjorno.billib.rest.model.TrendList;
import com.adjorno.billib.rest.model.Trends;
import com.m14n.billib.data.BB;
import com.m14n.ex.Ex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class TrendsController implements ITrendsController {
    private static final String PASSWORD = "vtldtlm";

    private static final int LIST_SIZE_PER_TYPE = 10;
    private static final int DB_LIST_SIZE_PER_TYPE = 5 * LIST_SIZE_PER_TYPE;

    @Autowired
    private TrendTypeRepository mTrendTypeRepository;

    @Autowired
    private TrackRepository mTrackRepository;

    @Autowired
    private WeekRepository mWeekRepository;

    @Autowired
    private ChartTrackRepository mChartTrackRepository;

    @Autowired
    private ChartListRepository mChartListRepository;

    @Autowired
    private TrendTrackRepository mTrendTrackRepository;

    @Autowired
    private TrackController mTrackController;

    @RequestMapping(value = "/trends", method = RequestMethod.GET)
    public Trends getTrendsAPI(
            @RequestParam(required = false) @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) String date) {
        final Week theWeek =
                date == null ? mChartListRepository.findLast(1L, PageRequest.of(0, 1)).getContent().get(0).getWeek()
                        : mWeekRepository.findByDate(BB.CHART_DATE_FORMAT.format(date));
        final List<TrendTrack> theTrendTracks = mTrendTrackRepository.findTrendsOfTheWeek(theWeek);
        final Map<Long, TrendList> theTrendLists = new HashMap<>();
        for (TrendTrack theTrendTrack : theTrendTracks) {
            final long trendType = theTrendTrack.getType().getId();
            final TrendList theTrendList =
                    theTrendLists.getOrDefault(trendType, new TrendList(theTrendTrack.getType().getDescription()));
            if (theTrendList.getTracks().size() < LIST_SIZE_PER_TYPE) {
                theTrendList.getTracks().add(theTrendTrack.getTrack());
            }
            theTrendLists.put(trendType, theTrendList);
        }
        final Trends theTrends = new Trends(
                theWeek.getDate(),
                theTrendLists.values().toArray(new TrendList[0])
        );
        return theTrends;
    }

    @RequestMapping(value = "/generateTrends", method = RequestMethod.POST)
    public void generateTrendsAPI(@RequestParam() String password,
                                  @RequestParam() @DateTimeFormat(pattern = BB.CHART_DATE_FORMAT_STRING) String week,
                                  @RequestParam(required = false, defaultValue = "0") int type) {
        if (!PASSWORD.equals(password)) {
            return;
        }
        generateTrends(week, type);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void generateTrends(String week, long type) {
        System.out.println("STARTED GENERATING TRENDS " + week);
        Week theWeek = mWeekRepository.findByDate(week);
        if (week != null) {
            if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_GAINERS) {
                generateGainers(theWeek);
            }
            if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_DEBUTS) {
                generateDebuts(theWeek);
            }
            if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_FUTURES) {
                generateFutures(theWeek);
            }
            if (type == TrendType.TYPE_ALL || type == TrendType.TYPE_SENIORS) {
                generateSeniors(theWeek);
            }
        }
        System.out.println("FINISHED GENERATING TRENDS");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void generateDebuts(Week week) {
        System.out.println("STARTED GENERATE DEBUTS");
        final List<Object[]> theBestDebuts = filterDebutsByCharts(
                mChartTrackRepository.findDebuts(week.getId()),
                blacklistedCharts()
        );
//there is an issue in Spring JPQL language, sort object can not recognize alias
//new Sort(Sort.Direction.DESC, "rating"));
        Collections.sort(theBestDebuts, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return Integer.compare((Integer) o2[1], (Integer) o1[1]);
            }
        });
        final TrendType theDebutsType = mTrendTypeRepository.findById(TrendType.TYPE_DEBUTS).orElse(null);
        Set<Long> theDebuts = new HashSet<>();
        for (ChartTrack theChartTrack : Ex.asListOfObjects(theBestDebuts, ChartTrack.class)) {
            if (!theDebuts.contains(theChartTrack.getTrack().getId())) {
                mTrendTrackRepository.save(new TrendTrack(null, week, theChartTrack.getTrack(), theDebutsType));
                theDebuts.add(theChartTrack.getTrack().getId());
            }
        }
        System.out.println("FINISHED GENERATE DEBUTS");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void generateFutures(Week week) {
        System.out.println("STARTED GENERATE FUTURES");
        List<ChartTrack> theChartTracks = filterByCharts(
                mChartTrackRepository.findByWeek(week),
                blacklistedCharts()
        );
        Collections.sort(theChartTracks, new Comparator<ChartTrack>() {
            @Override
            public int compare(ChartTrack o1, ChartTrack o2) {
                if (o1.getLastWeekRank() == 0) {
                    if (o2.getLastWeekRank() == 0) {
                        return 0;
                    }
                    return 1;
                }
                if (o2.getLastWeekRank() == 0) {
                    return -1;
                }
                int theResult =
                        Integer.compare(o1.getRank() - o1.getLastWeekRank(), o2.getRank() - o2.getLastWeekRank());
                if (theResult != 0) {
                    return theResult;
                }
                theResult = Integer.compare(o1.getRank(), o2.getRank());
                if (theResult != 0) {
                    return theResult;
                }
                return Integer.compare(o1.getChartList().getChart().getListSize(),
                        o2.getChartList().getChart().getListSize());
            }
        });
        final TrendType theFuturesType = mTrendTypeRepository.findById(TrendType.TYPE_FUTURES).orElse(null);
        for (int i = 0; i < theChartTracks.size() && i < DB_LIST_SIZE_PER_TYPE; i++) {
            mTrendTrackRepository.save(new TrendTrack(null, week, theChartTracks.get(i).getTrack(), theFuturesType));
        }
        System.out.println("FINISHED GENERATE FUTURES");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void generateSeniors(Week week) {
        System.out.println("STARTED GENERATE SENIORS");
        List<ChartTrack> theChartTracks = filterByCharts(
                mChartTrackRepository.findByWeek(week),
                blacklistedCharts()
        );
        List<Track> theTracks = mTrackRepository
                .sortByGlobalRank(TrackUtils.asTrackIds(TrackUtils.asTracks(theChartTracks)), DB_LIST_SIZE_PER_TYPE);
        final TrendType theSeniorsType = mTrendTypeRepository.findById(TrendType.TYPE_SENIORS).orElse(null);
        for (Track theTrack : theTracks) {
            mTrendTrackRepository.save(new TrendTrack(null, week, theTrack, theSeniorsType));
        }
        System.out.println("FINISHED GENERATE SENIORS");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void generateGainers(Week week) {
        System.out.println("STARTED GENERATE GAINERS");
        final TrendType theGainersType = mTrendTypeRepository.findById(TrendType.TYPE_GAINERS).orElse(null);
        //mTrendTrackRepository.deleteByWeekAndType(week, theGainersType);
        List<ChartTrack> theChartTracks = filterByCharts(
                mChartTrackRepository.findByWeek(week),
                blacklistedCharts()
        );
        List<Track> theTracks = TrackUtils.asTracks(theChartTracks);
        List<Long> theTrackIds = new ArrayList<>(new HashSet<>(TrackUtils.asTrackIds(theTracks)));
        System.out.println("UNIQUE TRACKS " + theTrackIds.size());
        Map<Long, Long> theGainerCache = new HashMap<>();
        Collections.sort(theTrackIds, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return Long.compare(getOrCreateGainerCache(o2, theGainerCache, mTrackController, week),
                        getOrCreateGainerCache(o1, theGainerCache, mTrackController, week));
            }
        });
        for (int i = 0; i < DB_LIST_SIZE_PER_TYPE && i < theTrackIds.size(); i++) {
            final Long theTrackId = theTrackIds.get(i);
            mTrendTrackRepository
                    .save(new TrendTrack(null, week, TrackUtils.findTrack(theTracks, theTrackId), theGainersType));
        }
        System.out.println("FINISHED GENERATE GAINERS");
    }

    private static Long getOrCreateGainerCache(Long trackId, Map<Long, Long> cache, TrackController trackController,
                                               Week week) {
        if (cache.containsKey(trackId)) {
            return cache.get(trackId);
        } else {
            Map<String, Map<String, Integer>> theTrackHistory = trackController.getTrackHistory(trackId, 0L);
            long gainerValue = 1;
            for (Map<String, Integer> chartHistory : theTrackHistory.values()) {
                List<Map.Entry<String, Integer>> theSortedHistory = new ArrayList<>(chartHistory.entrySet());
                Collections.sort(theSortedHistory, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o2.getKey().compareTo(o1.getKey());
                    }
                });
                if (theSortedHistory.get(0).getKey().equals(week.getDate())) {
                    for (int i = 1; i < theSortedHistory.size(); i++) {
                        Integer theValue = theSortedHistory.get(i).getValue();
                        if ((theValue == 1) || (theValue < theSortedHistory.get(i - 1).getValue())) {
                            break;
                        }
                        if (i + 1 > gainerValue) {
                            gainerValue = i + 1;
                        }
                    }
                }
            }
            cache.put(trackId, gainerValue);
            System.out.println(String.format("CACHE %d: %d %d", cache.size(), trackId, gainerValue));
            return gainerValue;
        }
    }

    private static List<ChartTrack> filterByCharts(List<ChartTrack> original, List<Long> blacklisted) {
        final List<ChartTrack> result = new ArrayList<>();
        for (ChartTrack chartTrack : original) {
            if (!blacklisted.contains(chartTrack.getChartList().getChart().getId())) {
                result.add(chartTrack);
            }
        }
        return result;
    }

    private static List<Object[]> filterDebutsByCharts(List<Object[]> original, List<Long> blacklisted) {
        final List<Object[]> result = new ArrayList<>();
        for (Object[] chartTrack : original) {
            if (!blacklisted.contains(((ChartTrack) chartTrack[0]).getChartList().getChart().getId())) {
                result.add(chartTrack);
            }
        }
        return result;
    }

    private static List<Long> blacklistedCharts() {
        return Arrays.asList(13L /*Japan*/, 17L /*Gospel*/, 18L /*Christian*/);
    }
}
